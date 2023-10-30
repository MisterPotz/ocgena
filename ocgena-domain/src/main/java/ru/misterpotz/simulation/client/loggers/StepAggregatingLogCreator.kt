package ru.misterpotz.simulation.client.loggers

import model.ActiveFiringTransition
import model.ExecutedBinding
import ru.misterpotz.model.marking.Time
import ru.misterpotz.simulation.logging.LogEvent
import ru.misterpotz.simulation.logging.LoggingEvent
import simulation.SimulationStateProvider
import javax.inject.Inject

class StepAggregatingLogCreator @Inject constructor(
    private val simulationStateProvider: SimulationStateProvider,
    private val transitionStartLoggerDelegate: TransitionStartLoggerDelegate,
    private val transitionEndLoggerDelegate: TransitionEndLoggerDelegate
) {
    private val step get() = simulationStateProvider.getSimulationStepState().currentStep
    private val simTime get() = simulationStateProvider.getSimulationTime().globalTime
    private val currentPMarking get() = simulationStateProvider.getOcNetState().pMarking

    fun onStart(): LoggingEvent {
        return LoggingEvent(
            step,
            logEvent = LogEvent.SIMULATION_START,
            simTime = simTime,
            currentMarking = null,
        )
    }

    fun afterInitialMarking(): LoggingEvent {
        val currentMarking = currentPMarking

        return LoggingEvent(
            step,
            logEvent = LogEvent.INITIAL_MARKING,
            simTime = simTime,
            currentMarking = currentMarking.toImmutable(),
        )
    }

    fun onExecutionNewStepStart(): LoggingEvent {
        return LoggingEvent(
            step = step,
            logEvent = LogEvent.SIMULATION_STEP_START,
            simTime = simTime,
            currentMarking = currentPMarking.toImmutable()
        )
    }

    fun beforeStartingNewTransitions(): LoggingEvent? {
        return null
    }

    fun onStartTransition(transition: ActiveFiringTransition): LoggingEvent? {
        transitionStartLoggerDelegate.applyDelta(
            transition.lockedObjectTokens
        )
        // TODO: create log here if more verbose log setting is used
        return null
    }

    fun afterStartingNewTransitions(): LoggingEvent {
        val accumulatedLockedTokens = transitionStartLoggerDelegate.getAccumulatedChange()

        val loggingEvent = LoggingEvent(
            step = step,
            logEvent = LogEvent.STARTED_TRANSITIONS,
            simTime = simTime,
            currentMarking = null,
            lockedTokens = accumulatedLockedTokens
        )

        transitionStartLoggerDelegate.clear()
        return loggingEvent
    }

    fun beforeEndingTransitions(): LoggingEvent? {
        return null
    }

    fun onEndTransition(executedBinding: ExecutedBinding) : LoggingEvent? {
        transitionEndLoggerDelegate.applyDelta(executedBinding.producedMap)
        return null
    }

    fun afterEndingTransitions(): LoggingEvent {
        val accChange = transitionEndLoggerDelegate.getAccumulatedChange()
        transitionEndLoggerDelegate.clear()
        return LoggingEvent(
            step = step,
            logEvent = LogEvent.ENDED_TRANSITIONS,
            simTime = simTime,
            unlockedTokens = accChange
        )
    }

    fun onExecutionStepFinish(newTimeDelta: Time) : LoggingEvent? {
        return null
    }

    fun afterFinalMarking(): LoggingEvent {
        return LoggingEvent(
            step = step,
            logEvent = LogEvent.INITIAL_MARKING,
            simTime = simTime,
            currentMarking = currentPMarking.toImmutable()
        )
    }

    fun onTimeout(): LoggingEvent {
        return LoggingEvent(
            step = step,
            logEvent = LogEvent.SIMULATION_TIMEOUT,
            simTime = simTime,
            currentMarking = currentPMarking.toImmutable()
        )
    }

    fun onEnd(): LoggingEvent {
        return LoggingEvent(
            step = step,
            logEvent = LogEvent.SIMULATION_END,
            simTime = simTime,
            currentMarking = currentPMarking.toImmutable()

        )
    }
}
