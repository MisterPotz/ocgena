package simulation.client.loggers

import model.ActiveFiringTransition
import model.ExecutedBinding
import ru.misterpotz.model.ObjectMarking
import model.Time
import simulation.Logger
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.state.SimulationTime

class CompoundLogger(
    override val loggingEnabled: Boolean,
    val loggers: List<Logger>
) : Logger {
    private inline fun log(crossinline block: Logger.() -> Unit) {
        if (loggingEnabled) {
            for (i in loggers) {
                i.block()
            }
        }
    }

    override fun onStart() {
        log { onStart() }
    }

    override fun onInitialMarking(marking: ObjectMarking) {
        log { onInitialMarking(marking) }
    }

    override fun onFinalMarking(marking: ObjectMarking) {
        log { onFinalMarking(marking) }
    }

    override fun onEnd() {
        log { onEnd() }
    }

    override fun onTimeout() {
        log { onTimeout() }
    }

    override fun onTimeShift(delta: Time) {
        log { onTimeShift(delta) }
    }

    override fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime
    ) {
        log { onExecutionStepStart(stepIndex, state, simulationTime) }
    }

    override fun onTransitionEndSectionStart() {
        log { onTransitionEndSectionStart() }
    }

    override fun beforeStartingNewTransitions() {
        log { beforeStartingNewTransitions() }
    }

    override fun onEndTransition(executedBinding: ExecutedBinding) {
        log { onEndTransition(executedBinding) }
    }

    override fun onStartTransition(transition: ActiveFiringTransition) {
        log { onStartTransition(transition) }
    }

}
