package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.ocnet.utils.defaultObjTypeId
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PrePlaceRegistry
import ru.misterpotz.ocgena.registries.TransitionsRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.SimulationStateProvider
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupCreatorFactory
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation.di.GlobalTokenBunch
import ru.misterpotz.ocgena.simulation.interactors.*
import ru.misterpotz.ocgena.simulation.state.PMarkingProvider
import ru.misterpotz.ocgena.utils.TimePNRef
import javax.inject.Inject
import kotlin.IllegalStateException
import kotlin.random.Random

class TimePetriNetStepExecutor @Inject constructor(
    private val newTimeDeltaInteractor: NewTimeDeltaInteractor,
    val ocNet: OCNet,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionToFireSelector: TransitionToFireSelector,
    private val transitionFireExecutor: TransitionFiringRuleExecutor,
) : StepExecutor {
    override suspend fun executeStep(executionContinuation: ExecutionContinuation) {
        // generate next time
        newTimeDeltaInteractor.generateAndShiftTimeDelta()

        // find transitions that are enforced to fire now, and list their random order
        val transitionsThatCanBeSelectedForFiring = buildList {
            val transitionsThatMustFireNow = ocNet.transitionsRegistry.iterable.filter {
                timePNTransitionMarking.forTransition(it.id).mustFireNow()
            }
            addAll(transitionsThatMustFireNow)

            if (transitionsThatMustFireNow.isEmpty()) {
                val transitionsThatCanFireNow = ocNet.transitionsRegistry.filter {
                    timePNTransitionMarking.forTransition(it.id).canFireNow()
                }
                addAll(transitionsThatCanFireNow)
            }
        }.sortedBy { it.id }

        // select the transition to fire
        val transitionToFire = transitionToFireSelector.select(transitionsThatCanBeSelectedForFiring) ?: return

        // fire transition using transition rule
        transitionFireExecutor.fireTransition(transitionToFire)
    }
}

class TransitionToFireSelector @Inject constructor(val random: Random?) {
    fun select(transitions: List<Transition>): Transition? {
        if (transitions.isEmpty()) return null
        return transitions.let {
            if (random == null) {
                it.random()
            } else {
                it.random(random)
            }
        }
    }
}

@TimePNRef("h(t)=#")
class TransitionDisabledChecker @Inject constructor(
    private val prePlaceRegistry: PrePlaceRegistry,
    @GlobalTokenBunch
    private val globalTokenBunch: SparseTokenBunch,
) {
    fun transitionIsDisabled(transition: PetriAtomId): Boolean {
        return prePlaceRegistry.transitionPrePlaces(transition) > globalTokenBunch.tokenAmountStorage()
    }
}

class TimeShiftSelector @Inject constructor(val random: Random?) {
    fun selectTimeDelta(max: Long): Long {
        return (1..max).let {
            if (random == null) {
                it.random()
            } else {
                it.random(random)
            }
        }
    }
}

class NewTimeDeltaInteractor @Inject constructor(
    private val timeShiftSelector: TimeShiftSelector,
    private val maxTimeDeltaFinder: MaxTimeDeltaFinder,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val simulationStateProvider: SimulationStateProvider
) {
    fun generateAndShiftTimeDelta() {
        val maxPossibleTimeDelta = maxTimeDeltaFinder.findMaxPossibleTimeDelta()
        if (maxPossibleTimeDelta != null && maxPossibleTimeDelta > 0) {
            simulationStateProvider.getSimulationStepState().onHasEnabledTransitions(true)
            val timeDelta = timeShiftSelector.selectTimeDelta(maxPossibleTimeDelta)
            timePNTransitionMarking.appendClockTime(timeDelta)
        }
    }
}

@TimePNRef("elapsing of time")
class MaxTimeDeltaFinder @Inject constructor(
    private val transitionsRegistry: TransitionsRegistry,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionDisabledChecker: TransitionDisabledChecker,
) {
    fun findMaxPossibleTimeDelta(): Long? {
        val partiallyEnabledTransitions = transitionsRegistry.iterable.filter { transition ->
            !transitionDisabledChecker.transitionIsDisabled(transition.id)
        }
        val minimumLftTransition = partiallyEnabledTransitions.minByOrNull { transition ->
            timePNTransitionMarking.forTransition(transition.id).timeUntilLFT()
        } ?: return null

        val transitionData = timePNTransitionMarking.forTransition(minimumLftTransition.id)

        @TimePNRef("tau")
        val timeDelta = transitionData.timeUntilLFT()
        return timeDelta
    }
}

interface SparseTokenBunch {
    fun objectMarking(): PlaceToObjectMarking
    fun tokenAmountStorage(): TokenAmountStorage
    fun append(tokenBunch: SparseTokenBunch)
    fun minus(tokenBunch: SparseTokenBunch)
}

class GlobalSparseTokenBunch(
    private val pMarkingProvider: PMarkingProvider,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
) : SparseTokenBunch {
    override fun objectMarking(): PlaceToObjectMarking {
        return pMarkingProvider.get()
    }

    override fun tokenAmountStorage(): TokenAmountStorage {
        return objectTokenRealAmountRegistry
    }

    override fun append(tokenBunch: SparseTokenBunch) {
        pMarkingProvider.get().plus(tokenBunch.objectMarking())
        objectTokenRealAmountRegistry.plus(tokenBunch.tokenAmountStorage())
    }

    override fun minus(tokenBunch: SparseTokenBunch) {
        pMarkingProvider.get().minus(tokenBunch.objectMarking())
        objectTokenRealAmountRegistry.minus(tokenBunch.tokenAmountStorage())
    }
}

class ImmutableSparseTokenBunchImpl(
    val marking: ImmutablePlaceToObjectMarking,
) : SparseTokenBunch {
    override fun objectMarking(): PlaceToObjectMarking {
        return marking
    }

    override fun tokenAmountStorage(): TokenAmountStorage {
        return marking
    }

    override fun append(tokenBunch: SparseTokenBunch) {
        throw IllegalStateException()
    }

    override fun minus(tokenBunch: SparseTokenBunch) {
        throw IllegalStateException()
    }

}

class SparseTokenBunchImpl(
    val marking: PlaceToObjectMarking = PlaceToObjectMarkingMap(),
    val tokenAmountStorage: SimpleTokenAmountStorage = SimpleTokenAmountStorage(),
) : SparseTokenBunch {
    override fun objectMarking(): PlaceToObjectMarking {
        return marking
    }

    override fun tokenAmountStorage(): TokenAmountStorage {
        return tokenAmountStorage
    }

    override fun append(tokenBunch: SparseTokenBunch) {
        objectMarking().plus(tokenBunch.objectMarking())
        tokenAmountStorage().plus(tokenBunch.tokenAmountStorage())
    }

    override fun minus(tokenBunch: SparseTokenBunch) {
        objectMarking().minus(tokenBunch.objectMarking())
    }

    fun reindex() {
        tokenAmountStorage.reindexFrom(marking)
    }

    interface Builder {
        fun forPlace(petriAtomId: PetriAtomId, block: PlaceAccessa.() -> Unit): Builder
        fun buildTokenBunch(): SparseTokenBunchImpl
        fun buildWithTypeRegistry(): Pair<SparseTokenBunchImpl, PlaceToObjectTypeRegistry>
    }

    private class BuilderImpl : Builder {
        val forPlace = mutableMapOf<PetriAtomId, PlaceAccessaImpl>()

        class PlaceAccessaImpl : PlaceAccessa {
            override var realTokens: Int = 0

            override val initializedTokens: MutableSet<ObjectTokenId> = mutableSetOf()
            var realType: ObjectTypeId? = null
            override var type: ObjectTypeId
                get() = realType!!
                set(value) {
                    realType = value
                }
        }

        override fun forPlace(petriAtomId: PetriAtomId, block: PlaceAccessa.() -> Unit): Builder {
            forPlace.getOrPut(petriAtomId) {
                PlaceAccessaImpl()
            }.block()
            return this
        }

        override fun buildTokenBunch(): SparseTokenBunchImpl {
            return SparseTokenBunchImpl(
                tokenAmountStorage = SimpleTokenAmountStorage(
                    placeToTokens = forPlace.mapValues { (id, block) ->
                        block.realTokens
                    }.toMutableMap(),
                ),
                marking = forPlace.mapValues { (id, block) ->
                    block.initializedTokens.toSortedSet()
                }.let {
                    PlaceToObjectMarkingMap(it.toMutableMap())
                }
            )
        }

        override fun buildWithTypeRegistry(): Pair<SparseTokenBunchImpl, PlaceToObjectTypeRegistry> {
            val sparseTokenBunch = buildTokenBunch()
            val placeToObjectTypeRegistry = PlaceToObjectTypeRegistry(
                defaultObjTypeId,
                placeIdToObjectType = forPlace.mapValues { (_, block) ->
                    block.realType ?: defaultObjTypeId
                }.toMutableMap()
            )
            return Pair(sparseTokenBunch, placeToObjectTypeRegistry)
        }
    }

    interface PlaceAccessa {
        var realTokens: Int
        val initializedTokens: MutableSet<ObjectTokenId>
        var type: ObjectTypeId
    }

    companion object {
        fun makeBuilder(builda: Builder.() -> Unit): Builder {
            val builder = BuilderImpl()
            builder.builda()
            return builder
        }
    }
}


fun ImmutablePlaceToObjectMarking.toImmutableBunch(): SparseTokenBunch {
    return ImmutableSparseTokenBunchImpl(this)
}

class TransitionTokenSelector(
    private val transitionPrePlaceAccessor: PrePlaceRegistry.PrePlaceAccessor,
    val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
) {
    val transition = transitionPrePlaceAccessor.transitionId

    fun selectAndInstantiateFrom(tokenBunch: SparseTokenBunch): SparseTokenBunch {
        val grabRecorder = SparseTokenBunchImpl()
        for (preplace in transitionPrePlaceAccessor) {
            val selectTokensToGrab = selectTokensToGrab(preplace, tokenBunch)
            grabRecorder.objectMarking()[preplace] = selectTokensToGrab.selected
        }
        grabRecorder.reindex()
        return grabRecorder
    }

    private fun selectTokensToGrab(
        place: PetriAtomId,
        tokenBunch: SparseTokenBunch,
    ): TokenSelectionInteractor.SelectedAndGeneratedTokens {
        val arcMultiplicity = arcsMultiplicityRegistry
            .transitionInputMultiplicityDynamic(place.arcIdTo(transition))

        val requiredTokensAmount = arcMultiplicity.requiredTokenAmount(tokenBunch.tokenAmountStorage())

        val selectedAndInitializedTokens = tokenSelectionInteractor.selectAndInitializeTokensFromPlace(
            petriAtomId = place,
            amount = requiredTokensAmount,
            tokenBunch = tokenBunch
        )

        return selectedAndInitializedTokens
    }
}

class Transition

@TimePNRef("firing")
class TransitionFiringRuleExecutor @Inject constructor(
    private val prePlaceRegistry: PrePlaceRegistry,
    @GlobalTokenBunch
    private val globalTokenBunch: SparseTokenBunch,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val tokenGroupCreatorFactory: TokenGroupCreatorFactory,
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val repeatabilityInteractor: RepeatabilityInteractor
) {
    fun fireTransition(transition: Transition) {
        // grab tokens captured by this transition
        val t_pre = prePlaceRegistry.transitionPrePlaces(transition.id)
        val transitionTokenSelector = TransitionTokenSelector(
            t_pre,
            arcsMultiplicityRegistry,
            tokenSelectionInteractor
        )
        // deduct those tokens from marking
        val selectedTokens = transitionTokenSelector.selectAndInstantiateFrom(globalTokenBunch)

        globalTokenBunch.minus(selectedTokens)

        // group the grabbed tokens
        val groupedTokenInfo = tokenGroupCreatorFactory
            .create(transition)
            .group(selectedTokens)

        // using the grouped tokens, get output tokens of this transition
        val transitionOutputTokensCreator = TransitionOutputTokensCreator(
            tokenGroupedInfo = groupedTokenInfo,
            transition = transition,
            arcsMultiplicityRegistry = arcsMultiplicityRegistry,
            transitionTokenSelectionInteractor = transitionTokenSelectionInteractor,
            placeToObjectTypeRegistry = placeToObjectTypeRegistry,
            repeatabilityInteractor = repeatabilityInteractor
        )
        val outputTokenBunch = transitionOutputTokensCreator.createOutputTokens()

        // append those to marking
        globalTokenBunch.append(outputTokenBunch)
    }
}

class TransitionOutputTokensCreator(
    private val tokenGroupedInfo: TokenGroupedInfo,
    private val transition: Transition,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val repeatabilityInteractor: RepeatabilityInteractor
) {
    fun createOutputTokens(): SparseTokenBunch {
        val outputPlaces = transition.toPlaces
        val outputMarking = PlaceToObjectMarking()

        val outputPlacesCorrected = repeatabilityInteractor.sortPlaces(outputPlaces)

        for (outputPlace in outputPlacesCorrected) {
            val outputArcId = transition.id.arcIdTo(outputPlace)

            val arcMultiplicity = arcsMultiplicityRegistry.transitionOutputMultiplicityDynamic(outputArcId)
            val sourceBuffer = arcMultiplicity.getTokenSourceForThisArc(tokenGroupedInfo)
            val tokensToConsume = arcMultiplicity.requiredTokenAmount(tokenGroupedInfo)

            // randomly selecting tokens from transition token buffer
            val selectedTokens =
                sourceBuffer
                    ?.let { transitionTokenSelectionInteractor.selectTokensFromBuffer(it, tokensToConsume) }

            if (selectedTokens != null) {
                sourceBuffer.removeAll(selectedTokens)
                outputMarking[outputPlace].addAll(selectedTokens)
            }
            val existingTokens = outputMarking[outputPlace]
            val tokensLeftToGenerate = tokensToConsume - existingTokens.size

            // generating missing tokens to fulfill output arcs numbers
            if (tokensLeftToGenerate > 0) {
                val outputPlaceType = placeToObjectTypeRegistry[outputPlace]

                val generatedTokens =
                    transitionTokenSelectionInteractor.generateTokens(outputPlaceType, tokensLeftToGenerate)

                outputMarking[outputPlace].addAll(generatedTokens)
            }
        }

        val tokenBunch = SparseTokenBunchImpl(
            marking = outputMarking,
            tokenAmountStorage = SimpleTokenAmountStorage()
        )
        tokenBunch.reindex()
        return tokenBunch
    }
}

interface TimePNTransitionMarking {
    fun forTransition(petriAtomId: PetriAtomId): TimePnTransitionData

    fun appendClockTime(delta: Long)
}

fun TimePNTransitionMarking(): TimePNTransitionMarkingImpl {
    return TimePNTransitionMarkingImpl(mutableMapOf())
}

class TimePNTransitionMarkingImpl @Inject constructor(
    private val mutableMap: MutableMap<PetriAtomId, TimePnTransitionData> = mutableMapOf(),
) : TimePNTransitionMarking {
    init {
        println("timepntransitionmarking instantiated")
    }

    override fun forTransition(petriAtomId: PetriAtomId): TimePnTransitionData {
        return mutableMap[petriAtomId]!!
    }

    override fun appendClockTime(delta: Long) {
        for (transition in mutableMap.values) {
            transition.incrementCounter(delta)
        }
    }
}

data class TimePnTransitionData(
    @TimePNRef("h(t)")
    var counter: Long,
    @TimePNRef("longest firing time")
    var lft: Long = 0,
    @TimePNRef("earliest firing time")
    var eft: Long = 0,
) {

    fun timeUntilLFT(): Long {
        return lft - counter
    }

    fun countup(units: Long) {
        counter = (counter + units).coerceAtLeast(lft)
    }

    fun updateBoundsAndReset(lft: Long, eft: Long) {
        require(eft <= lft)
        this.lft = lft
        this.eft = eft
        counter = 0
    }

    fun incrementCounter(delta: Long) {
        this.counter += delta
    }

    fun resetCounter() {
        counter = 0
    }

    fun mustFireNow(): Boolean {
        return counter >= lft
    }

    fun canFireNow(): Boolean {
        return counter >= eft
    }
}