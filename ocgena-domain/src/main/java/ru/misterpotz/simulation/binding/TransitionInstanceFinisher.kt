package simulation.binding

import model.ExecutedBinding
import model.TransitionInstance
import ru.misterpotz.model.marking.ImmutableObjectMarking
import simulation.Logger
import simulation.SimulationStateProvider
import javax.inject.Inject

interface TransitionInstanceFinisher {
    fun finishActiveTransition(activeFiringTransition: TransitionInstance)
}

class TransitionInstanceFinisherImpl @Inject constructor(
    private val inputToOutputPlaceResolver: InputToOutputPlaceResolver,
    private val logger: Logger,
    private val simulationStateProvider: SimulationStateProvider
) : TransitionInstanceFinisher {
    private val simulationTime get() = simulationStateProvider.getSimulationTime()
    private val pMarking get() = simulationStateProvider.getOcNetState().pMarking

    override fun finishActiveTransition(activeFiringTransition: TransitionInstance) {
        val markingForOutputPlaces = getMarkingForOutputPlaces(activeFiringTransition)

        // TODO: think what to do with the synchronization time
//        markingForOutputPlaces.shiftTokenTime(tokenTimeDelta = activeFiringTransition.duration)

        pMarking.plus(markingForOutputPlaces)

        val executedBinding = ExecutedBinding(
            activeFiringTransition,
            consumedMap = activeFiringTransition.lockedObjectTokens,
            producedMap = markingForOutputPlaces,
            finishedTime = simulationTime.globalTime
        )
        logger.onEndTransition(executedBinding)
    }

    private fun getMarkingForOutputPlaces(activeFiringTransition: TransitionInstance): ImmutableObjectMarking {
        return inputToOutputPlaceResolver.createOutputMarking(activeFiringTransition)
    }
}
