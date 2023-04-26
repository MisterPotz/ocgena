package simulation

import model.*

interface LoggerFactory {
    fun create(
        labelsActivities: LabelsActivities
    ): Logger
}

object LoggerFactoryDefault : LoggerFactory {
    override fun create(labelsActivities: LabelsActivities): Logger {
        return DebugTracingLogger(
            labelsActivities = labelsActivities,
            logCurrentState = true
        )
    }

}

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
        simulationTime: SimulationTime
    )

    fun onTransitionEndSectionStart()

    fun onTransitionStartSectionStart()

    fun onTransitionEnded(executedBinding: ExecutedBinding)

    fun onTransitionStart(transition: ActiveFiringTransition)
}
