package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PrePlaceRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupCreatorFactory
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation.di.GlobalTokenBunch
import ru.misterpotz.ocgena.simulation.interactors.RepeatabilityInteractor
import ru.misterpotz.ocgena.simulation.interactors.SimpleTokenAmountStorage
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import ru.misterpotz.ocgena.simulation.state.PMarkingProvider
import ru.misterpotz.ocgena.simulation.structure.SimulatableOcNetInstance
import ru.misterpotz.ocgena.utils.TimePNRef
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class TimePetriNetStepExecutor(
    private val newTimeDeltaInteractor: NewTimeDeltaInteractor,
    val ocNet: OCNet,
    @GlobalTokenBunch
    private val sparseTokenBunch: GlobalSparseTokenBunch,
    private val simulatableOcNetInstance: SimulatableOcNetInstance,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionToFireSelector: TransitionToFireSelector,
    private val prePlaceRegistry: PrePlaceRegistry,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    private val state:
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

        // select transition
        val transitionToFire = transitionToFireSelector.select(transitionsThatCanBeSelectedForFiring) ?: return

        // select tokens to go into the transition
        val tokenGrabber = TransitionTokenGrabber(
            prePlaceRegistry.transitionPrePlaces(transitionToFire.id),
            arcsMultiplicityRegistry = arcsMultiplicityRegistry,
            tokenSelectionInteractor = tokenSelectionInteractor
        )
        tokenGrabber.grabFrom(sparseTokenBunch)
        val grabbedTokens = tokenGrabber.result()

        // fire transition using transition rule


    }
}

class TransitionToFireSelector(val random: Random?) {
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
class TransitionDisabledChecker(
    private val prePlaceRegistry: PrePlaceRegistry,
    private val tokenAmountStorage: TokenAmountStorage,
) {
    fun transitionIsDisabled(transition: PetriAtomId): Boolean {
        return prePlaceRegistry.transitionPrePlaces(transition) > tokenAmountStorage
    }
}

class TimeShiftSelector(val random: Random?) {
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

class NewTimeDeltaInteractor(
    private val timeShiftSelector: TimeShiftSelector,
    private val maxTimeDeltaFinder: MaxTimeDeltaFinder,
    private val timePNTransitionMarking: TimePNTransitionMarking,
) {
    fun generateAndShiftTimeDelta() {
        val maxPossibleTimeDelta = maxTimeDeltaFinder.findMaxPossibleTimeDelta()
        val timeDelta = timeShiftSelector.selectTimeDelta(maxPossibleTimeDelta)
        timePNTransitionMarking.appendClockTime(timeDelta)
    }
}

@TimePNRef("elapsing of time")
class MaxTimeDeltaFinder(
//    val state: State,
    private val ocNet: OCNet,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionDisabledChecker: TransitionDisabledChecker,
) {
    fun findMaxPossibleTimeDelta(): Long {
        ocNet.transitionsRegistry.iterable.filter { transition ->
            !transitionDisabledChecker.transitionIsDisabled(transition.id)
        }
        val minimumLftTransition = ocNet.transitionsRegistry.iterable.minBy { transition ->
            timePNTransitionMarking.forTransition(transition.id).lft
        }

        val transitionData = timePNTransitionMarking.forTransition(minimumLftTransition.id)

        @TimePNRef("tau")
        val timeDelta = transitionData.timeUntilLFT()
        return timeDelta
    }
}

interface SparseTokenBunch {
    fun objectMarking(): PlaceToObjectMarking
    fun tokenAmountStorage(): TokenAmountStorage
    fun appendWithAmount(tokenBunch: SparseTokenBunch)
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

    override fun appendWithAmount(tokenBunch: SparseTokenBunch) {

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

    override fun appendWithAmount(tokenBunch: SparseTokenBunch) {
        objectMarking().plus(tokenBunch.objectMarking())
        tokenAmountStorage().plus(tokenBunch.tokenAmountStorage())
    }

    fun reindex() {
        tokenAmountStorage.reindexFrom(marking)
    }
}

class TransitionTokenGrabber(
    private val transitionPrePlaceAccessor: PrePlaceRegistry.PrePlaceAccessor,
    val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
) {
    val transition = transitionPrePlaceAccessor.transitionId
    private val grabRecorder: SparseTokenBunch = SparseTokenBunchImpl()

    fun grabFrom(tokenBunch: SparseTokenBunch) {
        val tokenBunchModifier = TokenBunchModifier(tokenBunch)

        for (preplace in transitionPrePlaceAccessor) {
            val selectTokensToGrab = selectTokensToGrab(preplace, tokenBunch)

            tokenBunchModifier.appendWithoutAmount(preplace, selectTokensToGrab.generated)

            grabRecorder.objectMarking()[preplace] = selectTokensToGrab.selected
        }

        tokenBunchModifier.removeWithAmount(grabRecorder)
    }

    fun result(): SparseTokenBunch {
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

class TransitionToken

class Transition

class TokenBunchModifier @Inject constructor(
    @GlobalTokenBunch
    private val tokenBunch: SparseTokenBunch,
) {
    fun appendWithoutAmount(
        place: PetriAtomId,
        tokens: SortedSet<ObjectTokenId>,
    ) {
        tokenBunch.objectMarking()[place].addAll(tokens)
    }

    fun removeWithAmount(
        maskTokenBunch: SparseTokenBunch,
    ) {
        val maskMarking = maskTokenBunch.objectMarking()
        val modifiedMarking = tokenBunch.objectMarking()

        for (i in maskMarking.places) {
            modifiedMarking[i] = modifiedMarking[i].apply {
                val atMaskMarking = maskMarking[i]

                removeAll(atMaskMarking)

                tokenBunch.tokenAmountStorage().applyDeltaTo(i, -atMaskMarking.size)
            }
        }
    }

    fun appendWithAmount(
        appendBunch: SparseTokenBunch,
    ) {
        val maskMarking = appendBunch.objectMarking()
        val modifiedMarking = tokenBunch.objectMarking()

        for (i in maskMarking.places) {
            modifiedMarking[i] = modifiedMarking[i].apply {
                val atMaskMarking = maskMarking[i]

                addAll(maskMarking[i])

                tokenBunch.tokenAmountStorage().applyDeltaTo(i, atMaskMarking.size)
            }
        }
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
    private val transitionTokenSelectionInteractor: TokenSelectionInteractor,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val repeatabilityInteractor: RepeatabilityInteractor
) {
    fun fireTransition(transition: Transition) {
        // grab tokens captured by this transition
        val t_pre = prePlaceRegistry.transitionPrePlaces(transition.id)
        val transitionTokenGrabber = TransitionTokenGrabber(
            t_pre,
            arcsMultiplicityRegistry,
            tokenSelectionInteractor
        )
        // deduct those tokens from marking
        transitionTokenGrabber.grabFrom(globalTokenBunch)

        val grabbedTokens = transitionTokenGrabber.result()

        // group the grabbed tokens
        val tokenGroupCreator = tokenGroupCreatorFactory.create(transition)
        tokenGroupCreator.group(grabbedTokens)
        val groupedTokenInfo = tokenGroupCreator.getGroupedInfo()

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

            val selectedTokens =
                sourceBuffer
                    ?.let { transitionTokenSelectionInteractor.selectTokensFromBuffer(it, tokensToConsume) }

            if (selectedTokens != null) {
                sourceBuffer.removeAll(selectedTokens)
                outputMarking[outputPlace].addAll(selectedTokens)
            }
            val existingTokens = outputMarking[outputPlace]
            val tokensLeftToGenerate = tokensToConsume - existingTokens.size

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


class TimePNTransitionMarking(
    private val mutableMap: MutableMap<PetriAtomId, TimePnTransitionData>,
) {

    fun forTransition(petriAtomId: PetriAtomId): TimePnTransitionData {
        return mutableMap[petriAtomId]!!
    }

    fun appendClockTime(delta: Long) {
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