package simulation

import model.ActiveFiringTransition
import model.ExecutedBinding
import utils.mprintln

class DebugLogger : Logger {
    override val loggingEnabled: Boolean
        get() = true

    fun String.indent(times: Int): String {
        return this.prependIndent(
            (0 until times).fold("") { accum, ind ->
                accum + "\t"
            }
        )
    }

    override fun onTimeout() {
        mprintln("execution timeout")
    }

    override fun onStart() {
        mprintln("execution started")
    }

    override fun onEnd() {
        mprintln("execution ended")
    }

    override fun onExecutionStep(stepIndex: Int) {
        mprintln("execution step: $stepIndex".indent(1))
    }

    override fun onTransitionEndSectionStart() {
        mprintln("""ending transitions:""".indent(2))
    }

    override fun onTransitionStartSectionStart() {
        mprintln("""starting transitions:""".indent(2))
    }

    override fun onTransitionEnded(executedBinding: ExecutedBinding) {
        mprintln(executedBinding.toString().indent(3))
    }

    override fun onTransitionStart(transition: ActiveFiringTransition) {
        mprintln(transition.toString().trimMargin().indent(3))
    }
}
