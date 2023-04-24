package simulation

import model.ActiveFiringTransition
import model.ExecutedBinding

interface Logger {
    val loggingEnabled: Boolean

    fun onStart()

    fun onEnd()
    fun onTimeout()

    abstract fun onExecutionStep(stepIndex: Int)

    fun onTransitionEndSectionStart()

    fun onTransitionStartSectionStart()

    fun onTransitionEnded(executedBinding: ExecutedBinding)

    fun onTransitionStart(transition: ActiveFiringTransition)
}
