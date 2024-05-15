package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.Shuffler
import ru.misterpotz.ocgena.simulation_v2.entities.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SortedTokens
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenStore
import ru.misterpotz.ocgena.simulation_v2.utils.sstep

interface ShiftTimeSelector {
    suspend fun get(timeRange: LongRange): Long
}

interface TransitionSelector {
    suspend fun get(transitions: Transitions): TransitionWrapper
}

interface TransitionSolutionSelector {
    suspend fun get(solutionMeta: ArcSolver.Meta): Int
}

interface ArcChecker {
    fun checkEnoughTokens(
        tokenSlice: TokenSlice
    ): Boolean
}

interface ArcSolver {
    fun getSolution(index: Int): Solution?

    data class Meta(val solutionsAmount: Int)
    data class Solution(
        val tokensRemoved: TokenSlice,
        val tokensAppended: TokenSlice,
        val garbagedTokens: SortedTokens
    )
}

interface ArcLogicsFactory {

    fun getArcChecker(
        ocNet: OCNet,
        prePlaces: Places,
        toTransition: TransitionWrapper
    ): ArcChecker

    fun getArcSolver(
        ocNet: OCNet,
        prePlaces: Places,
        toTransition: TransitionWrapper,
    ): ArcSolver

    object Stub : ArcLogicsFactory {
        override fun getArcChecker(ocNet: OCNet, prePlaces: Places, toTransition: TransitionWrapper): ArcChecker {
            TODO("Not yet implemented")
        }

        override fun getArcSolver(ocNet: OCNet, prePlaces: Places, toTransition: TransitionWrapper): ArcSolver {
            TODO("Not yet implemented")
        }
    }
}

class StepExecutor(
    private val transitions: Transitions,
    private val places: Places,
    private val shiftTimeSelector: ShiftTimeSelector,
    private val transitionSelector: TransitionSelector,
    private val transitionSolutionSelector: TransitionSolutionSelector,
    private val tokenStore: TokenStore,
    private val model: ModelAccessor,
    private val shuffler: Shuffler,
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

                val hasSolution = transition.inputArcsSolutions(
                    tokenStore,
                    shuffler = shuffler,
                ).iterator().hasNext()

                transition.setEnabledByMarkingCache(hasSolution)
            }
        }
    }

    suspend fun execute() {
        sstep("Need to reindex transitions that are not indexed") {
            reindexTransitionSolutions()
        }

        val simulationFinish = sstep("try finding shift time and increment clocks") {
            // определяем какие transition enabled
            val enabledByMarkign = collectTransitions(EnabledMode.ENABLED_BY_MARKING)

            val shiftTimes = determineShiftTimes(enabledByMarkign) ?: return@sstep true

            val shiftTime = selectShiftTime(shiftTimes)
            enabledByMarkign.forEach { t -> t.timer.incrementCounter(shiftTime) }
            false
        }
        if (simulationFinish) return

        sstep("execute transition if possible") {
            val fireableTransitions = collectTransitions(EnabledMode.CAN_FIRE)
            val selectedTransition = selectTransitionToFire(fireableTransitions)

            fireTransition(selectedTransition)

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
            cleanGarbageTokens()
        }
    }

    private fun cleanTokenTransitionVisits(token: TokenWrapper) {
        for (visitedTransition in token.visitedTransitions) {
            visitedTransition.removeTokenVisit(token)
        }
    }

    private fun cleanTokenTransitionVisits(tokens: Collection<TokenWrapper>) {
        for (token in tokens) {
            cleanTokenTransitionVisits(token)
        }
    }

    private fun cleanGarbageTokens() {
        model.outPlaces.forEach { endPlace ->
            cleanTokenTransitionVisits(tokenStore.tokensAt(endPlace))
            tokenStore.modifyTokensAt(endPlace) { tokens ->
                tokens.forEach { tokenStore.removeToken(it) }
                tokens.clear()
            }
        }
    }

    private fun fireTransition(transition: TransitionWrapper) {
        val solution = transition.inputArcsSolutions(tokenStore, shuffler)
            .iterator().also {
                require(it.hasNext()) {
                    "during transition fire there must already exist solution that was checked before"
                }
            }
            .next()
        val minusTokens = solution.toTokenSlice()
        val (plusTokens, consumed) = transition.outputArcsSolutions(minusTokens, shuffler, tokenGenerator = tokenStore)

        tokenStore.minus(minusTokens)
        tokenStore.plus(plusTokens)

        transition.logTokensOnFireIfSynchronized(plusTokens)
        transition.transitionsWithSharedPreplaces.forEach { t ->
            t.timer.resetCounter()
        }
        cleanTokenTransitionVisits(consumed)
        tokenStore.removeTokens(consumed)
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

    suspend fun selectTransitionToFire(transitions: Transitions): TransitionWrapper {
        return transitionSelector.get(transitions)
    }
}

