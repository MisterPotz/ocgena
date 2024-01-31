package ru.misterpotz.ocgena.simulation.stepexecutor

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation.SimulationTaskPreparator
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupCreatorFactory
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation.di.GlobalTokenBunch
import ru.misterpotz.ocgena.simulation.interactors.RepeatabilityInteractor
import ru.misterpotz.ocgena.simulation.interactors.SimpleTokenAmountStorage
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import ru.misterpotz.ocgena.simulation.stepexecutor.timepn.NewTimeDeltaInteractor
import ru.misterpotz.ocgena.utils.TimePNRef
import simulation.random.RandomSource
import javax.inject.Inject
import kotlin.random.Random


class TimePetriNetStepExecutor @Inject constructor(
    private val newTimeDeltaInteractor: NewTimeDeltaInteractor,
    val ocNet: OCNet,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionToFireSelector: TransitionToFireSelector,
    private val transitionFireExecutor: TransitionFiringRuleExecutor,
    private val prePlaceRegistry: PrePlaceRegistry,
    private val transitionsEnabledStepCache: TransitionsEnabledStepCache,
    private val transitionDisabledByMarkingChecker: TransitionDisabledByMarkingChecker,
) : StepExecutor {

    private fun collectTransitionsThatCanBeSelected(): List<Transition> {
        return buildList {
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
    }

    private fun resetClocksOfTransitionsWithSharedPreplaces(transition: Transition) {
        val t_pre = prePlaceRegistry.transitionPrePlaces(transition.id)
        val transitionsWithCommonPreplaces = t_pre.getTransitionsWithSharedPreplaces()
        for (transition in transitionsWithCommonPreplaces) {
            timePNTransitionMarking.forTransition(transition)
                .apply {
                    resetCounter()
                }
        }
    }

    private fun resetClocksOfTransitionsDisabledByMarking() {
        for (transition in transitionDisabledByMarkingChecker.transitionsDisabledByMarking()) {
            timePNTransitionMarking.forTransition(transition).resetCounter()
        }
    }



    override suspend fun executeStep(executionContinuation: ExecutionContinuation) {
        // generate next time based on place and transitions marking
        newTimeDeltaInteractor.generateAndShiftTimeDelta()

        // find transitions that are enforced to fire now, and list their random order
        val transitionsThatCanBeSelectedForFiring = collectTransitionsThatCanBeSelected()

        // select the transition to fire
        val transitionToFire = transitionToFireSelector.select(transitionsThatCanBeSelectedForFiring) ?: return

        // fire transition using transition rule
        transitionFireExecutor.fireTransition(transitionToFire)

        // clocks of transitions with shared preplaces are reset anyway
        resetClocksOfTransitionsWithSharedPreplaces(transitionToFire)

        // clocks of transitions that became disabled by marking are now reset
        resetClocksOfTransitionsDisabledByMarking()
    }
}

class TransitionToFireSelector @Inject constructor(private val randomSource: RandomSource) {
    fun select(transitions: List<Transition>): Transition? {
        if (transitions.isEmpty()) return null
        return transitions.random(randomSource.transitionSelection())
    }
}

@TimePNRef("h(t)=#")
class TransitionDisabledByMarkingChecker @Inject constructor(
    private val prePlaceRegistry: PrePlaceRegistry,
    @GlobalTokenBunch
    private val globalTokenBunch: SparseTokenBunch,
    private val transitionsRegistry: TransitionsRegistry
) {
    fun transitionIsDisabledByMarking(transition: PetriAtomId): Boolean {
        return prePlaceRegistry.transitionPrePlaces(transition) > globalTokenBunch.tokenAmountStorage()
    }

    fun transitionsPartiallyEnabledByMarking(): List<PetriAtomId> {
        return transitionsRegistry.iterable.mapNotNull { transition ->
            transition.id
                .takeIf { !transitionIsDisabledByMarking(transition.id) }
        }
    }

    fun transitionsDisabledByMarking(): List<PetriAtomId> {
        return transitionsRegistry.iterable.mapNotNull { transition ->
            transition.id
                .takeIf { transitionIsDisabledByMarking(transition = transition.id) }
        }
    }
}

class TimeShiftSelector @Inject constructor(private val randomSource: RandomSource) {
    @TimePNRef("tau")
    fun selectTimeDelta(possibleTimeRange: LongRange): Long {
        return possibleTimeRange.random(randomSource.timeSelection())
    }
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

class TransitionsEnabledStepCache @Inject constructor() {
    private val transitionToEnabled = mutableMapOf<PetriAtomId, Boolean>()
    fun compareAndSetEnabledByMarking(transition: PetriAtomId, isEnabledByMarking: Boolean): Boolean {
        val oldValue = transitionToEnabled[transition]
        transitionToEnabled[transition] = isEnabledByMarking
        return oldValue != isEnabledByMarking
    }

    fun getTransitionEnabledByMarking(transition: PetriAtomId): Boolean {
        return transitionToEnabled[transition]!!
    }
}

@TimePNRef("firing")
class TransitionFiringRuleExecutor @Inject constructor(
    private val prePlaceRegistry: PrePlaceRegistry,
    @GlobalTokenBunch
    private val globalTokenBunch: SparseTokenBunch,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val tokenGroupCreatorFactory: TokenGroupCreatorFactory,
    private val transitionsOutputTokensCreatorFactory: TransitionOutputTokensCreatorFactory,
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
        val transitionOutputTokensCreator = transitionsOutputTokensCreatorFactory.create(groupedTokenInfo, transition)
        val outputTokenBunch = transitionOutputTokensCreator.createOutputTokens()

        // append those to marking
        globalTokenBunch.append(outputTokenBunch)
    }
}

class DefaultTimePNProvider @Inject constructor() {
    fun getDefaultEarliestFiringTime(): Int {
        return 5
    }

    fun getDefaultLatestfiringTime(): Int {
        return 10
    }
}

class SimulationTaskTimePNPreparator @Inject constructor(
    private val simulationConfig: SimulationConfig,
    @GlobalTokenBunch
    private val tokenBunch: SparseTokenBunch,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val defaultTimePNProvider: DefaultTimePNProvider,
) : SimulationTaskPreparator {

    private val transitionsTimePNSpec: TransitionsTimePNSpec = simulationConfig.castTransitions()
    override fun prepare() {
        (simulationConfig.initialMarking ?: MarkingScheme()).placesToTokens.forEach { (petriAtomId, amount) ->
            tokenBunch.tokenAmountStorage().applyDeltaTo(petriAtomId, +amount)
        }
        for (transition in simulationConfig.ocNet.transitionsRegistry.iterable) {
            timePNTransitionMarking
                .forTransition(transition.id)
                .updateBoundsAndReset(
                    eft = (transitionsTimePNSpec.getForTransition(transition.id)?.earlyFiringTime
                        ?: defaultTimePNProvider.getDefaultEarliestFiringTime()).toLong(),
                    lft = (transitionsTimePNSpec.getForTransition(transition.id)?.latestFiringTime
                        ?: defaultTimePNProvider.getDefaultLatestfiringTime()).toLong(),
                )
        }
    }
}

class TransitionOutputTokensCreator @AssistedInject constructor(
    @Assisted
    private val tokenGroupedInfo: TokenGroupedInfo,
    @Assisted
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

@AssistedFactory
interface TransitionOutputTokensCreatorFactory {
    fun create(tokenGroupedInfo: TokenGroupedInfo, transition: Transition): TransitionOutputTokensCreator
}

interface TimePNTransitionMarking {
    fun forTransition(petriAtomId: PetriAtomId): TimePnTransitionData

    fun appendClockTime(transitionsToAppendTime: List<PetriAtomId>, delta: Long)
}

fun TimePNTransitionMarking(map: Map<PetriAtomId, TimePnTransitionData>): TimePNTransitionMarkingImpl {
    return TimePNTransitionMarkingImpl(map.toMutableMap())
}

fun TimePNTransitionMarking(list: List<PetriAtomId>): TimePNTransitionMarkingImpl {
    return TimePNTransitionMarkingImpl(
        list.associateWith { TimePnTransitionData(counter = 0, lft = 0, eft = 0) }
            .toMutableMap()
    )
}

data class TimePNTransitionMarkingImpl @Inject constructor(
    private val mutableMap: MutableMap<PetriAtomId, TimePnTransitionData> = mutableMapOf(),
) : TimePNTransitionMarking {
    init {
        println("timepntransitionmarking instantiated")
    }

    override fun forTransition(petriAtomId: PetriAtomId): TimePnTransitionData {
        return mutableMap[petriAtomId]!!
    }

    override fun appendClockTime(transitionsToAppendTime: List<PetriAtomId>, delta: Long) {
        for (transition in transitionsToAppendTime) {
            forTransition(transition).incrementCounter(delta)
        }
    }
}

data class TimePnTransitionData(
    @TimePNRef("h(t)", comment = "Not exactly h(t), but tracks the clocks of transitions")
    var counter: Long,
    @TimePNRef("earliest firing time")
    var eft: Long = 0,
    @TimePNRef("longest firing time")
    var lft: Long = 0,
) {

    fun timeUntilLFT(): Long {
        return lft - counter
    }

    fun timeUntilEft(): Long {
        return eft - counter
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