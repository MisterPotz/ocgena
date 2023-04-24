package simulation

import model.ANSI_PINK
import model.ANSI_ORANGE
import model.ActiveFiringTransition
import model.ExecutedBinding
import model.Time
import utils.indent
import utils.indentMargin
import utils.mprintln
import utils.print

class DebugLogger(
    val logCurrentState: Boolean = false,
) : Logger {
    override val loggingEnabled: Boolean
        get() = true

    override fun onTimeout() {
        mprintln("execution timeout")
    }

    override fun onStart() {
        mprintln("execution started")
    }

    override fun onEnd() {
        mprintln("execution ended")
    }

    override fun onTimeShift(delta: Time) {
        mprintln("${ANSI_PINK}on time shift +${delta.print()}".indent(2))
    }

    override fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime,
    ) {
        mprintln("execution step: $stepIndex".indent(1))
        mprintln("""${ANSI_ORANGE}time: $simulationTime""".indent(2))
        mprintln("""current state: """.indent(2, prefix = ""))
        mprintln(state.toString().indentMargin(3, margin = "*"))
    }

    override fun onTransitionEndSectionStart() {
        mprintln("""ending transitions:""".indent(2))
    }

    override fun onTransitionStartSectionStart() {
        mprintln("""starting transitions:""".indent(2))
    }

    override fun onTransitionEnded(executedBinding: ExecutedBinding) {
        mprintln(executedBinding.prettyPrintExecuted().indentMargin(3, margin = "x"))
    }

    override fun onTransitionStart(transition: ActiveFiringTransition) {
        mprintln(transition.prettyPrintStarted().trimMargin().indentMargin(3))
    }
}
