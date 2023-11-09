package ru.misterpotz.ocgena.simulation.binding

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation.interactors.TIOutputPlacesResolverInteractor
import ru.misterpotz.ocgena.simulation.logging.Logger
import ru.misterpotz.ocgena.simulation.SimulationStateProvider
import javax.inject.Inject

interface TIFinisher {
    fun finishActiveTransition(activeFiringTransition: TransitionInstance)
}

class TIFinisherImpl @Inject constructor(
    private val outputPlacesResolver: TIOutputPlacesResolverInteractor,
    private val logger: Logger,
    private val simulationStateProvider: SimulationStateProvider,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
) : TIFinisher {
    private val simulationTime get() = simulationStateProvider.getSimulationTime()
    private val pMarking get() = simulationStateProvider.simulatableOcNetInstance().state.pMarking

    override fun finishActiveTransition(activeFiringTransition: TransitionInstance) {
        val markingForOutputPlaces = getMarkingForOutputPlaces(activeFiringTransition)

        // TODO: think what to do with the synchronization time
//        markingForOutputPlaces.shiftTokenTime(tokenTimeDelta = activeFiringTransition.duration)

        markingForOutputPlaces.keys.forEach {  petriAtomId ->
            val tokenSize = markingForOutputPlaces[petriAtomId]!!.size
            objectTokenRealAmountRegistry.incrementRealAmountAt(petriAtomId, tokenSize)
        }
        pMarking.plus(markingForOutputPlaces)

        val executedBinding = ExecutedBinding(
            activeFiringTransition,
            consumedMap = activeFiringTransition.lockedObjectTokens,
            producedMap = markingForOutputPlaces,
            finishedTime = simulationTime.globalTime
        )
        logger.onEndTransition(executedBinding)
    }

    private fun getMarkingForOutputPlaces(activeFiringTransition: TransitionInstance): ImmutablePlaceToObjectMarking {
        return outputPlacesResolver.createOutputMarking(activeFiringTransition)
    }
}
