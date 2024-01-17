package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.registries.PrePlaceRegistry
import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.utils.TimePNRef

class TimePetriNetStepExecutor : StepExecutor {
    override suspend fun executeStep(executionContinuation: ExecutionContinuation) {

    }
}

@TimePNRef("elapsing of time")
class TimeElapser(
//    val state: State,
    val ocNet: OCNet,
    val tokenAmountStorage: TokenAmountStorage,
    val prePlaceRegistry: PrePlaceRegistry,
    val timePNTransitionMarking : TimePNTransitionMarking
) {
    fun findMinimumPossibleTimeDelta(): Long {
        ocNet.transitionsRegistry.iterable.filter { transition ->
            val prePlaces = prePlaceRegistry.transitionPrePlaces(transition.id)
            prePlaces <= tokenAmountStorage
        }
        for (i in ocNet.transitionsRegistry.iterable) {

        }
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
}

data class TimePnTransitionData(
    @TimePNRef("h(t)")
    var counter: Long,
    @TimePNRef("longest firing time")
    var lft: Long = 0,
    @TimePNRef("earliest firing time")
    var eft: Long = 0,
) {
    fun countup(units: Long) {
        counter = (counter + units).coerceAtLeast(lft)
    }

    fun updateBoundsAndReset(lft: Long, eft: Long) {
        require(eft <= lft)
        this.lft = lft
        this.eft = eft
        counter = 0
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