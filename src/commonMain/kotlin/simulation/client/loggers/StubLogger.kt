package simulation.client.loggers

import model.ActiveFiringTransition
import model.ExecutedBinding
import model.ObjectMarking
import model.Time
import simulation.Logger
import simulation.SimulatableComposedOcNet
import simulation.SimulationTime

abstract class StubLogger : Logger {
    override val loggingEnabled: Boolean = false
    override fun onStart() {

    }

    override fun onInitialMarking(marking: ObjectMarking) {

    }

    override fun onFinalMarking(marking: ObjectMarking) {

    }

    override fun onEnd() {

    }

    override fun onTimeout() {

    }

    override fun onTimeShift(delta: Time) {

    }

    override fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime
    ) {

    }

    override fun onTransitionEndSectionStart() {

    }

    override fun onTransitionStartSectionStart() {

    }

    override fun onTransitionEnded(executedBinding: ExecutedBinding) {

    }

    override fun onTransitionStart(transition: ActiveFiringTransition) {

    }

}
