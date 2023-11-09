package ru.misterpotz.ocgena.simulation_old.binding

import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarkingDelta
import ru.misterpotz.ocgena.simulation_old.interactors.TIOutputPlacesResolverInteractor
import ru.misterpotz.ocgena.simulation_old.logging.SimulationDBLogger
import ru.misterpotz.ocgena.simulation_old.SimulationStateProvider
import ru.misterpotz.ocgena.simulation_old.collections.TransitionInstance
import javax.inject.Inject

interface TIFinisher {
    fun finishActiveTransition(activeFiringTransition: TransitionInstance)
}

class TIFinisherImpl @Inject constructor(
    private val outputPlacesResolver: TIOutputPlacesResolverInteractor,
    private val simulationDBLogger: SimulationDBLogger,
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
            val tokenSize = markingForOutputPlaces[petriAtomId].size
            objectTokenRealAmountRegistry.incrementRealAmountAt(petriAtomId, tokenSize)
        }

        pMarking.plus(markingForOutputPlaces as PlaceToObjectMarkingDelta)

        val executedBinding = ExecutedBinding(
            activeFiringTransition,
            consumedMap = activeFiringTransition.lockedObjectTokens,
            producedMap = markingForOutputPlaces,
            finishedTime = simulationTime.globalTime
        )
        simulationDBLogger.onEndTransition(executedBinding)
    }

    private fun getMarkingForOutputPlaces(activeFiringTransition: TransitionInstance): ImmutablePlaceToObjectMarking {
        return outputPlacesResolver.createOutputMarking(activeFiringTransition)
    }
}
