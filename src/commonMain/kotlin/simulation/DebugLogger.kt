package simulation

import model.ActiveFiringTransition
import model.ExecutedBinding
import utils.indent
import utils.indentMargin
import utils.mprintln

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

    override fun onExecutionStepStart(stepIndex: Int, state: SimulatableComposedOcNet.State) {
        mprintln("execution step: $stepIndex".indent(1))
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
