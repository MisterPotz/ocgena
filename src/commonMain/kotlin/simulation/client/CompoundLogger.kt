package simulation.client

import model.ActiveFiringTransition
import model.ExecutedBinding
import model.ObjectMarking
import model.Time
import simulation.Logger
import simulation.SimulatableComposedOcNet
import simulation.SimulationTime

class CompoundLogger(
    override val loggingEnabled: Boolean,
    val loggers: Array<Logger>
) : Logger {
    private inline fun log(crossinline block: Logger.() -> Unit) {
        for (i in loggers) {
            i.block()
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

    override fun onTransitionStartSectionStart() {
        log { onTransitionStartSectionStart() }
    }

    override fun onTransitionEnded(executedBinding: ExecutedBinding) {
        log { onTransitionEnded(executedBinding) }
    }

    override fun onTransitionStart(transition: ActiveFiringTransition) {
        log { onTransitionStart(transition) }
    }

}
