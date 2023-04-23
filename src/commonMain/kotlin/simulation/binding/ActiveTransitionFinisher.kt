package simulation.binding

import model.ActiveFiringTransition
import model.ExecutedBinding
import model.ObjectMarking
import simulation.Logger

interface ActiveTransitionFinisher {
    fun finishActiveTransition(activeFiringTransition: ActiveFiringTransition)
}


class ActiveTransitionFinisherImpl(
    private val pMarking: ObjectMarking,
    private val inputToOutputPlaceResolver: InputToOutputPlaceResolver,
    private val logger: Logger
): ActiveTransitionFinisher {
    override fun finishActiveTransition(activeFiringTransition: ActiveFiringTransition) {
        val markingForOutputPlaces = getMarkingForOutputPlaces(activeFiringTransition)

        pMarking += markingForOutputPlaces

        val executedBinding = ExecutedBinding(
            activeFiringTransition,
            consumedMap = activeFiringTransition.lockedObjectTokens,
            producedMap = markingForOutputPlaces
        )
        logger.onTransitionEnded(executedBinding)
    }

    private fun getMarkingForOutputPlaces(activeFiringTransition: ActiveFiringTransition) : ObjectMarking {
        val transition = activeFiringTransition.transition
        val inputPlaces = transition.inputPlaces
        val tokens = activeFiringTransition.lockedObjectTokens

        val newMarking = ObjectMarking()
        for (inputPlace in inputPlaces) {
            val outputPlaceForInput =
                inputToOutputPlaceResolver.getOutputPlaceForInput(transition, inputPlace)

            newMarking[outputPlaceForInput] = tokens[inputPlace]!!
        }
        return newMarking
    }
}
