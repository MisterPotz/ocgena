package simulation

import model.ActiveFiringTransition
import model.ExecutedBinding
import model.ObjectMarking
import model.Time

interface Logger {
    val loggingEnabled: Boolean

    fun onStart()

    fun onInitialMarking(marking: ObjectMarking)
    fun onFinalMarking(marking: ObjectMarking)
    fun onEnd()
    fun onTimeout()

    fun onTimeShift(delta: Time)

    abstract fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime)

    fun onTransitionEndSectionStart()

    fun onTransitionStartSectionStart()

    fun onTransitionEnded(executedBinding: ExecutedBinding)

    fun onTransitionStart(transition: ActiveFiringTransition)
}
