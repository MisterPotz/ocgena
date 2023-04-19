package simulation

import model.ActiveBinding
import utils.mprintln

class DebugLogger : Logger {
    private val executedBindings: MutableList<ActiveBinding> = mutableListOf()
    override val loggingEnabled: Boolean
        get() = true

    override fun onStart() {
        mprintln("execution started")
    }

    override fun logBindingExecution(binding: ActiveBinding) {
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
