package ru.misterpotz.ocgena.simulation_old.logging.loggers

import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.simulation_old.binding.ExecutedBinding
import ru.misterpotz.ocgena.simulation_old.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation_old.logging.SimulationDBLogger

class CompoundSimulationDBLogger(
    val simulationDBLoggers: List<SimulationDBLogger>
) : NoOpSimulationDBLogger() {
    private inline fun log(crossinline block: SimulationDBLogger.() -> Unit) {
        for (i in simulationDBLoggers) {
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
