package simulation

import model.Binding
import utils.mprintln

class DebugLogger : Logger {
    private val executedBindings: MutableList<Binding> = mutableListOf()
    override val loggingEnabled: Boolean
        get() = true

    override fun onStart() {
        mprintln("execution started")
    }

    override fun logBindingExecution(binding: Binding) {
        mprintln("\texecute: ${binding.toString().prependIndent()}")
        executedBindings.add(binding)
    }

    override fun onEnd() {
        mprintln("execution ended, executed bindings: ${executedBindings.size}")
    }

    override fun onExecutionStep(stepIndex: Int) {
        mprintln("Execution step: $stepIndex")
    }
}
