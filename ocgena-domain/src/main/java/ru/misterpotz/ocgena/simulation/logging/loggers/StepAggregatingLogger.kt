package ru.misterpotz.ocgena.simulation.logging.loggers

import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.simulation.binding.ExecutedBinding
import ru.misterpotz.marking.objects.Time
import ru.misterpotz.simulation.logging.LoggingEvent
import simulation.Logger
import javax.inject.Inject

class StepAggregatingLogger @Inject constructor(
    private val logReceiver: StepAggregatingLogReceiver,
    private val stepAggregatingLogCreator: StepAggregatingLogCreator,
) : Logger {

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
}