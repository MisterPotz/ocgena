package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.registries.PrePlaceRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation.di.GlobalTokenBunch
import ru.misterpotz.ocgena.simulation.interactors.SimpleTokenAmountStorage
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import ru.misterpotz.ocgena.simulation.state.PMarkingProvider
import ru.misterpotz.ocgena.utils.TimePNRef
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class TimePetriNetStepExecutor(
    private val newTimeDeltaInteractor: NewTimeDeltaInteractor,
    val ocNet: OCNet,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionToFireSelector: TransitionToFireSelector,
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

        val transitionToFire = transitionToFireSelector.select(transitionsThatCanBeSelectedForFiring)

        // fire transition using transition rule



    }
}

class TransitionToFireSelector(val random: Random?) {
    fun select(transitions: List<Transition>): Transition {
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
}

class SparseTokenBunchImpl(
    val marking: PlaceToObjectMarkingMap = PlaceToObjectMarkingMap(),
    val tokenAmountStorage: TokenAmountStorage = SimpleTokenAmountStorage(),
) : SparseTokenBunch {
    override fun objectMarking(): PlaceToObjectMarking {
        return marking
    }

    override fun tokenAmountStorage(): TokenAmountStorage {
        return tokenAmountStorage
    }

}

class TransitionTokenGrabber(
    private val transitionPrePlaceAccessor: PrePlaceRegistry.PrePlaceAccessor,
    val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
) {
    val transition = transitionPrePlaceAccessor.transitionId
    val grabRecorder: SparseTokenBunch = SparseTokenBunchImpl()

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
        generatedTokens: SortedSet<ObjectTokenId>,
    ) {
        tokenBunch.objectMarking()[place].addAll(generatedTokens)
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
class TransitionFiringRuleExecutor(
    private val prePlaceRegistry: PrePlaceRegistry,
    @GlobalTokenBunch
    private val globalTokenBunch: SparseTokenBunch,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
) {
    fun fireTransition(transition: PetriAtomId) {
        // grab tokens captured by this transition
        val t_pre = prePlaceRegistry.transitionPrePlaces(transition)
        val transitionTokenGrabber = TransitionTokenGrabber(
            t_pre,
            arcsMultiplicityRegistry,
            tokenSelectionInteractor
        )
        // deduct those tokens from marking
        transitionTokenGrabber.grabFrom(globalTokenBunch)

        val grabbedTokens = transitionTokenGrabber.result()

        // group the grabbed tokens
        grabbedTokens

        // using that token bunch, get output tokens of this transition


        // append those to marking


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