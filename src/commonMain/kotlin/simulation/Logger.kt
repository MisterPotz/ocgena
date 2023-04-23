package simulation

import model.ActiveFiringTransition
import model.ExecutedBinding

interface Logger {
    val loggingEnabled: Boolean

    fun onStart()

    fun onEnd()
    abstract fun onExecutionStep(stepIndex: Int)

    fun onTransitionEnded(executedBinding: ExecutedBinding)

    fun onTransitionStart(transition: ActiveFiringTransition)
}
