package simulation

import model.Binding

interface Logger {
    val loggingEnabled: Boolean

    fun onStart()

    // TODO: pass in to visitors to log the performance
    fun logBindingExecution(binding: Binding)

    fun onEnd()
    abstract fun onExecutionStep(stepIndex: Int)
}
