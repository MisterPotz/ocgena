package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.PrePlaceRegistry
import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.utils.TimePNRef
import kotlin.random.Random

class TimePetriNetStepExecutor(
    private val newTimeDeltaInteractor: NewTimeDeltaInteractor,
    val ocNet: OCNet,
    private val timePNTransitionMarking: TimePNTransitionMarking,
    private val transitionToFireSelector: TransitionToFireSelector
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
    fun select(transitions : List<Transition>) : Transition {
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
    private val tokenAmountStorage: TokenAmountStorage
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

@TimePNRef("firing")
class TransitionFiringRuleExecutor(

) {
    fun fireTransition(transition: PetriAtomId) {

    }
}

class TimePNTransitionMarking(
    private val mutableMap: MutableMap<PetriAtomId, TimePnTransitionData>
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