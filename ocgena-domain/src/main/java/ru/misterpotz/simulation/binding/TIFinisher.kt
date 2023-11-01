package simulation.binding

import ru.misterpotz.simulation.binding.ExecutedBinding
import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.simulation.api.interactors.TIOutputPlacesResolverInteractor
import simulation.Logger
import simulation.SimulationStateProvider
import javax.inject.Inject

interface TIFinisher {
    fun finishActiveTransition(activeFiringTransition: TransitionInstance)
}

class TIFinisherImpl @Inject constructor(
    private val outputPlacesResolver: TIOutputPlacesResolverInteractor,
    private val logger: Logger,
    private val simulationStateProvider: SimulationStateProvider
) : TIFinisher {
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
        return outputPlacesResolver.createOutputMarking(activeFiringTransition)
    }
}
