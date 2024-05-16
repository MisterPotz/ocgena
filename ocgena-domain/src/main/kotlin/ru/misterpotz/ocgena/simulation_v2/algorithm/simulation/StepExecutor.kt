package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.SimulationStepLog
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.FullSolution
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.Shuffler
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.Transitions
import ru.misterpotz.ocgena.simulation_v2.entities.wrap
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenStore
import ru.misterpotz.ocgena.simulation_v2.utils.sstep

interface ShiftTimeSelector {
    suspend fun get(timeRange: LongRange): Long
}

interface TransitionSelector {
    suspend fun get(transitions: Transitions): TransitionWrapper
}

interface TransitionSolutionSelector {
    suspend fun get(solutionMeta: Meta): Int
    data class Meta(val solutionsAmount: Int)
}

class StepExecutor(
    private val transitions: Transitions,
    private val shiftTimeSelector: ShiftTimeSelector,
    private val transitionSelector: TransitionSelector,
    private val tokenStore: TokenStore,
    private val model: ModelAccessor,
    private val shuffler: Shuffler
) {
    enum class EnabledMode {
        DISABLED_BY_MARKING,
        ENABLED_BY_MARKING,
        CAN_FIRE,
    }

    private fun collectTransitions(
        filter: EnabledMode,
        sourceTransitions: Transitions? = null
    ): Transitions {
        return (sourceTransitions ?: transitions).filter {
            when (filter) {
                EnabledMode.CAN_FIRE -> {
                    it.timer.canFireNow() && it.checkEnabledByTokensCache()
                }

                EnabledMode.DISABLED_BY_MARKING -> {
                    !it.checkEnabledByTokensCache()
                }

                EnabledMode.ENABLED_BY_MARKING -> {
                    it.checkEnabledByTokensCache()
                }
            }
        }.wrap()
    }
    // index transitions

    private fun reindexTransitionSolutions() {
        for (transition in transitions) {
            if (transition.needCheckCache()) {

                val solutionIterator = transition.inputArcsSolutions(
                    tokenStore,
                    shuffler = shuffler,
                    tokenGenerator = tokenStore,
                    existenceConfirmationMode = true
                ).iterator()

                val hasSolution = solutionIterator.hasNext()
                if (hasSolution) {
                    val solution = solutionIterator.next()
                    // need to create additional logics to only calculate availability, where additional tokens are not generated...
                    when (solution) {
                        is FullSolution.Exists -> {
                            // ok
                        }

                        else -> throw IllegalStateException()
                    }
                }
                transition.setEnabledByMarkingCache(hasSolution)
            }
        }
    }

    var logBuilder: LogBuilder = LogBuilder()
    private var stepNumber: Long = 0

    suspend fun execute(): SimulationStepLog? {
        logBuilder = LogBuilder()
        logBuilder.recordStartMarking(tokenStore)
        logBuilder.recordStepNumber(stepNumber)

        sstep("Need to reindex transitions that are not indexed") {
            reindexTransitionSolutions()
        }

        val simulationFinish = sstep("try finding shift time and increment clocks") {
            // определяем какие transition enabled
            val enabledByMarkign = collectTransitions(EnabledMode.ENABLED_BY_MARKING)

            val shiftTimes = determineShiftTimes(enabledByMarkign) ?: return@sstep true

            val shiftTime = selectShiftTime(shiftTimes)
            enabledByMarkign.forEach { t -> t.timer.incrementCounter(shiftTime) }
            logBuilder.recordClockIncrement(shiftTime)
            false
        }
        if (simulationFinish) return null

        sstep("execute transition if possible") {
            val fireableTransitions = collectTransitions(EnabledMode.CAN_FIRE)
            val selectedTransition = selectTransitionToFire(fireableTransitions)

            fireTransition(selectedTransition)
            logBuilder.recordMarking(tokenStore)

            selectedTransition.setNeedCheckCache()
            for (transition in selectedTransition.dependentTransitions) {
                transition.setNeedCheckCache()
            }
        }

        sstep("reindex preliminary solutions of disabled transitions") {
            reindexTransitionSolutions()
        }

        sstep("reset clocks of transitions that became disabled") {
            collectTransitions(EnabledMode.DISABLED_BY_MARKING).forEach { t ->
                t.timer.resetCounter()
            }
        }

        sstep("clean tokens whose participation is finished") {
            garbageTokensAtEndPlace()
        }
        stepNumber++
        return logBuilder.build()
    }

    private fun cleanTokenTransitionVisits(tokens: Collection<TokenWrapper>) {
        for (token in tokens) {
            for (visitedTransition in token.visitedTransitions) {
                visitedTransition.removeTokenVisit(token)
            }
        }
    }

    private fun fireTransition(transition: TransitionWrapper) {
        logBuilder.recordFiredTransition(transition)
        val solution = transition.inputArcsSolutions(tokenStore, shuffler, tokenGenerator = tokenStore)
            .iterator().also {
                require(it.hasNext()) {
                    "during transition fire there must already exist solution that was checked before"
                }
            }
            .next()
        val generatedAtInputSolution = (solution as? FullSolution.Tokens)?.generatedTokens ?: emptyList()

        val minusTokens = solution.toTokenSlice()
        val (plusTokens, consumed, generatedAtOutput) = transition.outputArcsSolutions(
            minusTokens,
            shuffler,
            tokenGenerator = tokenStore
        )

        tokenStore.minus(minusTokens)
        tokenStore.plus(plusTokens)
        println("minusTokens ${minusTokens.makeBeautifulString()}")
        println("plusTokens ${plusTokens.makeBeautifulString()}")
        println("removed tokens $consumed")
        println("generated tokens ${generatedAtInputSolution + generatedAtOutput}")
        logBuilder.recordTokens(
            minusTokens,
            plusTokens,
            generatedAtInputSolution + generatedAtOutput
        )

        transition.logTokensOnFireIfSynchronized(plusTokens)
        transition.transitionsWithSharedPreplaces.forEach { t ->
            t.timer.resetCounter()
        }
        garbageTokens(consumed)
    }

    private fun garbageTokens(tokens: List<TokenWrapper>) {
        cleanTokenTransitionVisits(tokens)
        tokenStore.removeTokens(tokens)
    }

    private fun garbageTokensAtEndPlace() {
        model.outPlaces.forEach { endPlace ->
            cleanTokenTransitionVisits(tokenStore.tokensAt(endPlace))
            tokenStore.modifyTokensAt(endPlace) { tokens ->
                tokenStore.removeTokens(tokens)
                tokens.clear()
            }
        }
    }

    private fun determineShiftTimes(enabledByMarking: Transitions): LongRange? {

        if (enabledByMarking.isEmpty()) return null

        // find time until min lft
        val minTimeUntilLftTrans = enabledByMarking.minByOrNull {
            it.timer.timeUntilLFT()
        }!!.timer.timeUntilLFT()
        // find time until min eft
        val minTimeUntilEftTrans = enabledByMarking.minByOrNull {
            it.timer.timeUntilEFT()
        }!!.timer.timeUntilEFT()

        return minTimeUntilEftTrans..minTimeUntilLftTrans
    }

    private suspend fun selectShiftTime(shiftTimes: LongRange): Long {
        return shiftTimeSelector.get(shiftTimes)
    }

    private suspend fun selectTransitionToFire(transitions: Transitions): TransitionWrapper {
        return transitionSelector.get(transitions)
    }
}

