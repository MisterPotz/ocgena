package simulation

import model.ActiveBinding

interface Logger {
    val loggingEnabled: Boolean

    fun onStart()

    // TODO: pass in to visitors to log the performance
    fun logBindingExecution(binding: ActiveBinding)

    fun onEnd()
    abstract fun onExecutionStep(stepIndex: Int)
}
