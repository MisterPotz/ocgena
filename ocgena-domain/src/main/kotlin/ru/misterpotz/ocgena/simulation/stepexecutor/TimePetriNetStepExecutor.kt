package ru.misterpotz.ocgena.simulation.stepexecutor

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.misterpotz.DBLogger
import ru.misterpotz.ObjectTokenMeta
import ru.misterpotz.SimulationStepLog
import ru.misterpotz.ocgena.collections.ObjectTokenSet
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.SimulationStateProvider
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
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.stepexecutor.timepn.NewTimeDeltaInteractor
import ru.misterpotz.ocgena.utils.TimePNRef
import ru.misterpotz.ocgena.utils.buildMutableMap
import simulation.random.RandomSource
import javax.inject.Inject
import javax.inject.Provider
import kotlin.properties.Delegates

interface SimulationStepLogger {
    fun logClockIncrement(increment: Long)
    fun logCurrentStep()
    fun logCurrentMarking()
    fun logTransitionInTokens(tokenbunch: SparseTokenBunch)
    fun logTransitionOutTokens(tokenBunch: SparseTokenBunch)
    fun appendInstantiatedTokens(tokens: List<ObjectTokenId>)
    fun logTransitionToFire(petriAtomId: PetriAtomId)
    fun logStepEndMarking()
    suspend fun finishStepLog()
    fun logStepEndTimePNMarking()
}

class SimulationStepLoggerImpl @Inject constructor(
    @GlobalTokenBunch
    private val sparseTokenBunch: SparseTokenBunch,
    private val simulationStateProvider: SimulationStateProvider,
    private val dbLogger: DBLogger,
    private val timePNTransitionMarking: Provider<TimePNTransitionMarking>,
    private val developmentDebugConfig: DevelopmentDebugConfig
) :
    SimulationStepLogger {
    private val currentSimulationStepLogBuilder: SimulationStepLogBuilder
        get() = simulationStateProvider.simulationLogBuilder()

    override fun logClockIncrement(increment: Long) {
        currentSimulationStepLogBuilder.clockIncrement = increment
    }

    override fun logCurrentStep() {
        currentSimulationStepLogBuilder.stepNumber = simulationStateProvider.getSimulationStepState().currentStep
    }

    override fun logCurrentMarking() {
        currentSimulationStepLogBuilder.starterMarkingAmounts = sparseTokenBunch.tokenAmountStorage().dump()
    }
    override fun logTransitionInTokens(tokenbunch: SparseTokenBunch) {
        currentSimulationStepLogBuilder.firingInMarkingAmounts = tokenbunch.tokenAmountStorage().dump()
        currentSimulationStepLogBuilder.firingInMarkingTokens = tokenbunch.objectMarking().dumpTokens()
    }

    override fun logTransitionOutTokens(tokenBunch: SparseTokenBunch) {
        currentSimulationStepLogBuilder.firingOutMarkingAmounts = tokenBunch.tokenAmountStorage().dump()
        currentSimulationStepLogBuilder.firingOutMarkingTokens = tokenBunch.objectMarking().dumpTokens()
    }

    override fun appendInstantiatedTokens(tokens: List<ObjectTokenId>) {
        currentSimulationStepLogBuilder.appendInstantiatedTokens(tokens)
    }

    override fun logTransitionToFire(petriAtomId: PetriAtomId) {
        currentSimulationStepLogBuilder.selectedFiredTransition = petriAtomId
    }

    override fun logStepEndMarking() {
        if (developmentDebugConfig.dumpEndStateMarking) {
            currentSimulationStepLogBuilder.stepEndMarking = sparseTokenBunch.tokenAmountStorage().dump()
        }
    }

    override fun logStepEndTimePNMarking() {
        if (developmentDebugConfig.dumpTimePNTransitionMarking) {
            currentSimulationStepLogBuilder.timePNTransitionMarking = timePNTransitionMarking.get().dump()
        }
    }


    override suspend fun finishStepLog() {
        dbLogger.acceptStepLog(currentSimulationStepLogBuilder.build())
    }
}

class TimePetriNetStepExecutor @Inject constructor(
    private val newTimeDeltaInteractor: NewTimeDeltaInteractor,
    val ocNet: OCNet,
    @GlobalTokenBunch
    private val sparseTokenBunch: SparseTokenBunch,
    private val objectTokenSet: ObjectTokenSet,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionToFireSelector: TransitionToFireSelector,
    private val transitionFireExecutor: TransitionFiringRuleExecutor,
    private val prePlaceRegistry: PrePlaceRegistry,
    private val transitionDisabledByMarkingChecker: TransitionDisabledByMarkingChecker,
    private val simulationStepLogger: SimulationStepLogger,
    private val simulationStateProvider: SimulationStateProvider
) : StepExecutor {

    private fun collectTransitionsThatCanBeSelected(): List<Transition> {
        return buildList {
            transitionDisabledByMarkingChecker.transitionsPartiallyEnabledByMarking().forEach {
                if (timePNTransitionMarking.forTransition(it).mustFireNow() ||
                    timePNTransitionMarking.forTransition(it).canFireNow()
                ) {
                    add(ocNet.transitionsRegistry[it])
                }
            }
        }
            .distinctBy { it.id }
            .sortedBy { it.id }
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

    private fun cleanTokensAtFinish() {
        for (outputPlace in ocNet.outputPlaces.iterable) {
            val tokensAtFinishPlace = sparseTokenBunch.objectMarking()[outputPlace]
            sparseTokenBunch.objectMarking().removeAllPlaceTokens(outputPlace)
            if (tokensAtFinishPlace.isNotEmpty()) {
                objectTokenSet.removeAll(tokensAtFinishPlace)
            }
        }
    }


    override suspend fun executeStep(executionContinuation: ExecutionContinuation) {
        simulationStepLogger.logCurrentStep()
        simulationStepLogger.logCurrentMarking()
        // generate next time based on place and transitions marking
        newTimeDeltaInteractor.generateAndShiftTimeDelta()


        // find transitions that are enforced to fire now, and list their random order
        val transitionsThatCanBeSelectedForFiring = collectTransitionsThatCanBeSelected()

        // select the transition to fire
        val transitionToFire =
            transitionToFireSelector.select(transitionsThatCanBeSelectedForFiring) ?: return Unit.also {
                simulationStateProvider.getSimulationStepState().setFinished()
            }
        simulationStepLogger.logTransitionToFire(transitionToFire.id)

        // fire transition using transition rule
        transitionFireExecutor.fireTransition(transitionToFire)

        // clocks of transitions with shared preplaces are reset anyway
        resetClocksOfTransitionsWithSharedPreplaces(transitionToFire)

        // clocks of transitions that became disabled by marking are now reset
        resetClocksOfTransitionsDisabledByMarking()

        simulationStepLogger.logStepEndMarking()
        simulationStepLogger.logStepEndTimePNMarking()

        cleanTokensAtFinish()

        simulationStepLogger.finishStepLog()
    }
}

interface DeterminedTransitionSequenceProvider {
    fun getNextTransition(): PetriAtomId?
}

class SimpleDeterminedTransitionSequenceProvider(transitions: List<PetriAtomId> = listOf()) :
    DeterminedTransitionSequenceProvider {
    private val transitions = transitions.toMutableList()
    override fun getNextTransition(): PetriAtomId? {
        return transitions.removeFirstOrNull()
    }
}

class TransitionToFireSelector @Inject constructor(
    private val randomSource: RandomSource,
    private val determinedSequence: DeterminedTransitionSequenceProvider
) {
    fun select(transitions: List<Transition>): Transition? {
        if (transitions.isEmpty()) return null
        val nextTransition = determinedSequence.getNextTransition()
        if (nextTransition != null) {
            return transitions.find { it.id == nextTransition }!!
        }
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


class SimulationStepLogBuilder @Inject constructor(
    private val objectTokenSet: ObjectTokenSet
) {
    var stepNumber: Long by Delegates.notNull()
    var clockIncrement: Long by Delegates.notNull()
    var selectedFiredTransition: PetriAtomId? = null
    var starterMarkingAmounts: Map<PetriAtomId, Int> by Delegates.notNull()
    var stepEndMarking: Map<PetriAtomId, Int>? = null
    var firingInMarkingAmounts: Map<PetriAtomId, Int> by Delegates.notNull()
    var firingInMarkingTokens: Map<PetriAtomId, List<ObjectTokenId>> by Delegates.notNull()
    var firingOutMarkingAmounts: Map<PetriAtomId, Int> by Delegates.notNull()
    var firingOutMarkingTokens: Map<PetriAtomId, List<ObjectTokenId>> by Delegates.notNull()
    var tokensInitializedAtStep: List<ObjectTokenMeta> by Delegates.notNull()
    var timePNTransitionMarking : Map<PetriAtomId, Long>? = null

    private val instantiatedTokens = mutableListOf<ObjectTokenId>()
    fun appendInstantiatedTokens(tokens: List<ObjectTokenId>) {
        instantiatedTokens.addAll(tokens)
    }


    private var alreadyBuilt = false
    fun build(): SimulationStepLog {
        require(!alreadyBuilt)
        val mappedToTokenMeta = instantiatedTokens.map {
            ObjectTokenMeta(it, objectTokenSet[it]!!.objectTypeId)
        }
        tokensInitializedAtStep = mappedToTokenMeta


        return SimulationStepLog(
            stepNumber,
            clockIncrement,
            selectedFiredTransition,
            starterMarkingAmounts,
            stepEndMarking,
            firingInMarkingAmounts,
            firingOutMarkingAmounts,
            firingInMarkingTokens,
            firingOutMarkingTokens,
            timePNTransitionMarking,
            tokensInitializedAtStep,
        ).also { alreadyBuilt = true }
    }
}

class SimulationStepLogBuilderCreator @Inject constructor(
    private val simulationStepLogBuilderProvider: Provider<SimulationStepLogBuilder>
) {
    fun create(): SimulationStepLogBuilder {
        return simulationStepLogBuilderProvider.get()
    }
}


class TransitionTokenSelector(
    private val transitionPrePlaceAccessor: PrePlaceRegistry.PrePlaceAccessor,
    val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
) {
    val transition = transitionPrePlaceAccessor.transitionId

    fun selectAndInstantiateFrom(tokenBunch: SparseTokenBunch): Pair<SparseTokenBunch, List<ObjectTokenId>> {
        val grabRecorder = SparseTokenBunchImpl()
        val instantiatedTokens = mutableListOf<ObjectTokenId>()
        for (preplace in transitionPrePlaceAccessor) {
            val selectTokensToGrab = selectTokensToGrab(preplace, tokenBunch)
            grabRecorder.objectMarking()[preplace] = selectTokensToGrab.selected
            instantiatedTokens.addAll(selectTokensToGrab.generated)
        }
        grabRecorder.reindex()
        return Pair(grabRecorder, instantiatedTokens)
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

@TimePNRef("firing")
class TransitionFiringRuleExecutor @Inject constructor(
    private val prePlaceRegistry: PrePlaceRegistry,
    @GlobalTokenBunch
    private val globalTokenBunch: SparseTokenBunch,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val tokenGroupCreatorFactory: TokenGroupCreatorFactory,
    private val transitionsOutputTokensCreatorFactory: TransitionOutputTokensCreatorFactory,
    private val simulationStepLogger: SimulationStepLogger
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
        val (selectedTokens, instantiatedTokens) = transitionTokenSelector.selectAndInstantiateFrom(globalTokenBunch)

        simulationStepLogger.logTransitionInTokens(selectedTokens)
        simulationStepLogger.appendInstantiatedTokens(instantiatedTokens)

        globalTokenBunch.minus(selectedTokens)

        // group the grabbed tokens
        val groupedTokenInfo = tokenGroupCreatorFactory
            .create(transition)
            .group(selectedTokens)

        // using the grouped tokens, get output tokens of this transition
        val transitionOutputTokensCreator = transitionsOutputTokensCreatorFactory.create(groupedTokenInfo, transition)
        val (outputTokenBunch, outputInstantiatedTokens) = transitionOutputTokensCreator.createOutputTokens()

        simulationStepLogger.logTransitionOutTokens(outputTokenBunch)
        simulationStepLogger.appendInstantiatedTokens(outputInstantiatedTokens)

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
    fun createOutputTokens(): Pair<SparseTokenBunch, List<ObjectTokenId>> {
        val outputPlaces = transition.toPlaces
        val outputMarking = PlaceToObjectMarking()

        val outputPlacesCorrected = repeatabilityInteractor.sortPlaces(outputPlaces)
        val newlyGeneratedTokens = mutableListOf<ObjectTokenId>()
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
                val outputPlaceType = placeToObjectTypeRegistry[outputPlace]!!

                val generatedTokens =
                    transitionTokenSelectionInteractor.generateTokens(outputPlaceType, tokensLeftToGenerate)
                newlyGeneratedTokens.addAll(generatedTokens)
                outputMarking[outputPlace].addAll(generatedTokens)
            }
        }

        val tokenBunch = SparseTokenBunchImpl(
            marking = outputMarking,
            tokenAmountStorage = SimpleTokenAmountStorage()
        )
        tokenBunch.reindex()
        return Pair(tokenBunch, newlyGeneratedTokens)
    }
}

@AssistedFactory
interface TransitionOutputTokensCreatorFactory {
    fun create(tokenGroupedInfo: TokenGroupedInfo, transition: Transition): TransitionOutputTokensCreator
}

interface TimePNTransitionMarking {
    fun forTransition(petriAtomId: PetriAtomId): TimePnTransitionData

    fun appendClockTime(transitionsToAppendTime: List<PetriAtomId>, delta: Long)

    fun copyZeroClock(): TimePNTransitionMarking
    fun applySettingsBlock(settingBlock: SettingBlock.() -> Unit) : TimePNTransitionMarking
    fun dump() : Map<String, Long>

    interface SettingBlock {
        infix fun String.to(clock: Int)
    }
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

    override fun dump(): Map<String, Long> {
        return mutableMap.mapValues { it.value.counter }
    }

    override fun copyZeroClock(): TimePNTransitionMarking {
        return copy(
            mutableMap = buildMutableMap {
                for ((key, value) in mutableMap) {
                    put(key, value.copy(counter = 0))
                }
            }
        )
    }

    override fun applySettingsBlock(settingBlock: TimePNTransitionMarking.SettingBlock.() -> Unit): TimePNTransitionMarking {
        val settingBlockImpl = SettingBlockImpl(this)
        settingBlockImpl.settingBlock()
        return this
    }

    private class SettingBlockImpl(val marking: TimePNTransitionMarking) : TimePNTransitionMarking.SettingBlock {
        override fun String.to(clock: Int) {
            marking.forTransition(this).counter = clock.toLong()
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
        return (lft - counter).coerceAtLeast(0)
    }

    fun timeUntilEft(): Long {
        return (eft - counter).coerceAtLeast(0)
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