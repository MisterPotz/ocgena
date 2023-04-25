package simulation.binding

import model.ActiveFiringTransition
import model.ExecutedBinding
import model.ImmutableObjectMarking
import model.ObjectMarking
import simulation.Logger

interface ActiveTransitionMarkingFinisher {
    val pMarking: ObjectMarking

    fun finishActiveTransition(activeFiringTransition: ActiveFiringTransition)
}


class ActiveTransitionFinisherImpl(
    override val pMarking: ObjectMarking,
    private val inputToOutputPlaceResolver: InputToOutputPlaceResolver,
    private val logger: Logger,
): ActiveTransitionMarkingFinisher {
    override fun finishActiveTransition(activeFiringTransition: ActiveFiringTransition) {
        val markingForOutputPlaces = getMarkingForOutputPlaces(activeFiringTransition)

        markingForOutputPlaces.shiftTokenTime(tokenTimeDelta = activeFiringTransition.duration)

        pMarking += markingForOutputPlaces

        val executedBinding = ExecutedBinding(
            activeFiringTransition,
            consumedMap = activeFiringTransition.lockedObjectTokens,
            producedMap = markingForOutputPlaces
        )
        logger.onTransitionEnded(executedBinding)
    }

    private fun getMarkingForOutputPlaces(activeFiringTransition: ActiveFiringTransition) : ImmutableObjectMarking {
        return inputToOutputPlaceResolver.createOutputMarking(activeFiringTransition)
    }
}
