package ru.misterpotz.ocgena.simulation_old.logging.loggers

import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.simulation_old.binding.ExecutedBinding
import ru.misterpotz.ocgena.simulation_old.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation_old.logging.LogEvent
import ru.misterpotz.ocgena.simulation_old.logging.LoggingEvent
import ru.misterpotz.ocgena.simulation_old.state.CurrentSimulationDelegate
import javax.inject.Inject

class StepAggregatingLogCreator @Inject constructor(
    private val transitionStartLoggerDelegate: TransitionStartLoggerDelegate,
    private val transitionEndLoggerDelegate: TransitionEndLoggerDelegate,
    private val currentSimulationDelegate: CurrentSimulationDelegate
) : CurrentSimulationDelegate by currentSimulationDelegate {

    fun onStart(): LoggingEvent {
        return LoggingEvent(
            currentStep,
            logEvent = LogEvent.SIMULATION_START,
            simTime = simGlobalTime,
            currentMarking = null,
        )
    }

    fun afterInitialMarking(): LoggingEvent {
        val currentMarking = pMarking

        return LoggingEvent(
            currentStep,
            logEvent = LogEvent.INITIAL_MARKING,
            simTime = simGlobalTime,
            currentMarking = currentMarking.toImmutable(),
        )
    }

    fun onExecutionNewStepStart(): LoggingEvent {
        return LoggingEvent(
            step = currentStep,
            logEvent = LogEvent.SIMULATION_STEP_START,
            simTime = simGlobalTime,
            currentMarking = pMarking.toImmutable()
        )
    }

    fun beforeStartingNewTransitions(): LoggingEvent? {
        return null
    }

    fun onStartTransition(transition: TransitionInstance): LoggingEvent? {
        transitionStartLoggerDelegate.applyDelta(
            transition.lockedObjectTokens
        )
        // TODO: create log here if more verbose log setting is used
        return null
    }

    fun afterStartingNewTransitions(): LoggingEvent {
        val accumulatedLockedTokens = transitionStartLoggerDelegate.getAccumulatedChange()

        val loggingEvent = LoggingEvent(
            step = currentStep,
            logEvent = LogEvent.STARTED_TRANSITIONS,
            simTime = simGlobalTime,
            currentMarking = null,
            lockedTokens = accumulatedLockedTokens
        )

        transitionStartLoggerDelegate.clear()
        return loggingEvent
    }

    fun beforeEndingTransitions(): LoggingEvent? {
        return null
    }

    fun onEndTransition(executedBinding: ExecutedBinding): LoggingEvent? {
        transitionEndLoggerDelegate.applyDelta(executedBinding.producedMap)
        return null
    }

    fun afterEndingTransitions(): LoggingEvent {
        val accChange = transitionEndLoggerDelegate.getAccumulatedChange()
        transitionEndLoggerDelegate.clear()
        return LoggingEvent(
            step = currentStep,
            logEvent = LogEvent.ENDED_TRANSITIONS,
            simTime = simGlobalTime,
            unlockedTokens = accChange
        )
    }

    fun onExecutionStepFinish(newTimeDelta: Time): LoggingEvent? {
        return null
    }

    fun afterFinalMarking(): LoggingEvent {
        return LoggingEvent(
            step = currentStep,
            logEvent = LogEvent.INITIAL_MARKING,
            simTime = simGlobalTime,
            currentMarking = pMarking.toImmutable()
        )
    }

    fun onTimeout(): LoggingEvent {
        return LoggingEvent(
            step = currentStep,
            logEvent = LogEvent.SIMULATION_TIMEOUT,
            simTime = simGlobalTime,
            currentMarking = pMarking.toImmutable()
        )
    }

    fun onEnd(): LoggingEvent {
        return LoggingEvent(
            step = currentStep,
            logEvent = LogEvent.SIMULATION_END,
            simTime = simGlobalTime,
            currentMarking = pMarking.toImmutable()

        )
    }
}
