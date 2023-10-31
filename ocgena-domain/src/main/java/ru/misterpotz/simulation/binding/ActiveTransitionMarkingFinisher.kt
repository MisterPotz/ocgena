package simulation.binding

import model.OngoingActivity
import model.ExecutedBinding
import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.ObjectMarking
import simulation.Logger
import simulation.SimulationStateProvider
import javax.inject.Inject

interface ActiveTransitionMarkingFinisher {
    val pMarking: ObjectMarking

    fun finishActiveTransition(activeFiringTransition: OngoingActivity)
}


class ActiveTransitionFinisherImpl @Inject constructor(
    private val inputToOutputPlaceResolver: InputToOutputPlaceResolver,
    private val logger: Logger,
    private val simulationStateProvider: SimulationStateProvider
) : ActiveTransitionMarkingFinisher {
    val simulationTime get() = simulationStateProvider.getSimulationTime()
    override val pMarking get() = simulationStateProvider.getOcNetState().pMarking

    override fun finishActiveTransition(activeFiringTransition: OngoingActivity) {
        val markingForOutputPlaces = getMarkingForOutputPlaces(activeFiringTransition)

        markingForOutputPlaces.shiftTokenTime(tokenTimeDelta = activeFiringTransition.duration)

        pMarking += markingForOutputPlaces

        val executedBinding = ExecutedBinding(
            activeFiringTransition,
            consumedMap = activeFiringTransition.lockedObjectTokens,
            producedMap = markingForOutputPlaces,
            finishedTime = simulationTime.globalTime
        )
        logger.onEndTransition(executedBinding)
    }

    private fun getMarkingForOutputPlaces(activeFiringTransition: OngoingActivity): ImmutableObjectMarking {
        return inputToOutputPlaceResolver.createOutputMarking(activeFiringTransition)
    }
}
