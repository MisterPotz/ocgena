package simulation

import model.EnabledSimpleBinding
import utils.mprintln

class DebugLogger : Logger {
    private val executedBindings: MutableList<EnabledSimpleBinding> = mutableListOf()
    override val loggingEnabled: Boolean
        get() = true

    override fun onStart() {
        mprintln("execution started")
    }

    override fun logBindingExecution(binding: EnabledSimpleBinding) {
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
