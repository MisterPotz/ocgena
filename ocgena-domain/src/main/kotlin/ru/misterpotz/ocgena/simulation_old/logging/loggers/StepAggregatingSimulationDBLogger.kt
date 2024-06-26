package ru.misterpotz.ocgena.simulation_old.logging.loggers

import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.simulation_old.binding.ExecutedBinding
import ru.misterpotz.ocgena.simulation_old.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation_old.logging.LoggingEvent
import javax.inject.Inject

class StepAggregatingSimulationDBLogger @Inject constructor(
    private val logReceiver: StepAggregatingLogReceiver,
    private val stepAggregatingLogCreator: StepAggregatingLogCreator,
) : NoOpSimulationDBLogger() {

    private fun logIfCreates(block : StepAggregatingLogCreator.() -> LoggingEvent?) {
        stepAggregatingLogCreator.block()?.let {
            logReceiver.onEvent(it)
        }
    }

    override fun onStart() {
        logIfCreates {
            onStart()
        }
    }

    override fun afterInitialMarking() {
        logIfCreates {
            afterInitialMarking()
        }
    }

    override fun onExecutionNewStepStart() {
        logIfCreates {
            onExecutionNewStepStart()
        }
    }

    override fun beforeStartingNewTransitions() {
        logIfCreates {
            beforeEndingTransitions()
        }
    }

    override fun onStartTransition(transition: TransitionInstance) {
        logIfCreates {
            onStartTransition(transition)
        }
    }

    override fun afterStartingNewTransitions() {
        logIfCreates {
            afterStartingNewTransitions()
        }
    }

    override fun beforeEndingTransitions() {
        logIfCreates {
            beforeEndingTransitions()
        }
    }

    override fun onEndTransition(executedBinding: ExecutedBinding) {
        logIfCreates {
            onEndTransition(executedBinding)
        }
    }

    override fun afterEndingTransitions() {
        logIfCreates {
            afterEndingTransitions()
        }
    }

    override fun onExecutionStepFinish(newTimeDelta: Time) {
        logIfCreates {
            onExecutionStepFinish(newTimeDelta)
        }
    }

    override fun afterFinalMarking() {
        logIfCreates {
            afterFinalMarking()
        }
    }

    override fun onTimeout() {
        logIfCreates {
            onTimeout()
        }
    }

    override fun onEnd() {
        logIfCreates {
            onTimeout()
        }
    }

    override fun beforeRemovingTokensAtFinishPlace() {

    }

    override fun afterRemovingTokensAtFinishPlace() {

    }
}