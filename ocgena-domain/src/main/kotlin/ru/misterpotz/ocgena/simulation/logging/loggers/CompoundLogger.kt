package simulation.client.loggers

import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.simulation.binding.ExecutedBinding
import ru.misterpotz.ocgena.simulation.logging.loggers.NoOpLogger
import ru.misterpotz.ocgena.simulation.logging.Logger

class CompoundLogger(
    val loggers: List<Logger>
) : NoOpLogger() {
    private inline fun log(crossinline block: Logger.() -> Unit) {
        for (i in loggers) {
            i.block()
        }
    }

    override fun onStart() {
        log { onStart() }
    }

    override fun afterInitialMarking() {
        log { afterInitialMarking() }
    }

    override fun onExecutionNewStepStart() {
        log { onExecutionNewStepStart() }
    }

    override fun beforeStartingNewTransitions() {
        log { beforeStartingNewTransitions() }
    }

    override fun onStartTransition(transition: TransitionInstance) {
        log { onStartTransition(transition) }
    }

    override fun afterStartingNewTransitions() {
        log { afterStartingNewTransitions() }
    }

    override fun beforeEndingTransitions() {
        log { beforeEndingTransitions() }
    }

    override fun onEndTransition(executedBinding: ExecutedBinding) {
        log { onEndTransition(executedBinding) }
    }

    override fun afterEndingTransitions() {
        log { afterEndingTransitions() }
    }

    override fun onExecutionStepFinish(newTimeDelta: Time) {
        log { onExecutionStepFinish(newTimeDelta) }
    }

    override fun afterFinalMarking() {
        log { afterFinalMarking() }
    }

    override fun onTimeout() {
        log { onTimeout() }
    }

    override fun onEnd() {
        log { onEnd() }
    }
}
