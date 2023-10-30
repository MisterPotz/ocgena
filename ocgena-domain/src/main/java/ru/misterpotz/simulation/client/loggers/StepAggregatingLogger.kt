package ru.misterpotz.simulation.client.loggers

import model.ActiveFiringTransition
import model.ExecutedBinding
import ru.misterpotz.model.ObjectMarking
import model.Time
import ru.misterpotz.simulation.logging.LogEvent
import ru.misterpotz.simulation.logging.LoggingEvent
import simulation.Logger
import simulation.SimulationStateProvider
import javax.inject.Inject

interface StepAggregatingLogReceiver {
    fun onEvent(loggingEvent: LoggingEvent)
}

class TransitionStartLoggerDelegate {
    val objectMarkingDelta : ObjectMarking

    fun applyDelta(objectMarkingDelta  : ObjectMarking.Delta) {

    }
}
class StepAggregatingLogger @Inject constructor(
    val logReceiver: StepAggregatingLogReceiver,
    val simulationStateProvider: SimulationStateProvider
) : Logger {
    val step get() = simulationStateProvider.getSimulationStepState().currentStep
    val simTime get() = simulationStateProvider.getSimulationTime().globalTime
    val currentPMarking get() = simulationStateProvider.getOcNetState().pMarking

    override fun onStart() {
        logReceiver.onEvent(
            LoggingEvent(
                step,
                logEvent = LogEvent.SIMULATION_START,
                simTime = simTime,
                currentMarking = currentPMarking,
            )
        )
    }

    override fun afterInitialMarking() {
        logReceiver.onEvent(
            LoggingEvent(

            )
        )
        TODO("Not yet implemented")
    }

    override fun onExecutionNewStepStart() {
        TODO("Not yet implemented")
    }

    override fun beforeStartingNewTransitions() {
        TODO("Not yet implemented")
    }

    override fun onStartTransition(transition: ActiveFiringTransition) {
        TODO("Not yet implemented")
    }

    override fun afterStartingNewTransitions() {
        TODO("Not yet implemented")
    }

    override fun beforeEndingTransitions() {
        TODO("Not yet implemented")
    }

    override fun onEndTransition(executedBinding: ExecutedBinding) {
        TODO("Not yet implemented")
    }

    override fun afterEndingTransitions() {
        TODO("Not yet implemented")
    }

    override fun onExecutionStepFinish(newTimeDelta: Time) {
        TODO("Not yet implemented")
    }

    override fun afterFinalMarking() {
        TODO("Not yet implemented")
    }

    override fun onTimeout() {
        TODO("Not yet implemented")
    }

    override fun onEnd() {
        TODO("Not yet implemented")
    }

}