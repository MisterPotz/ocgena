package simulation

import model.*
import simulation.client.loggers.DebugTracingLogger

interface LoggerFactory {
    fun create(
        labelMapping: LabelMapping
    ): Logger
}

object LoggerFactoryDefault : LoggerFactory {
    override fun create(labelMapping: LabelMapping): Logger {
        return DebugTracingLogger(
            labelMapping = labelMapping,
            logCurrentState = true
        )
    }
}

object LoggerFactoryFileDefault : LoggerFactory {
    override fun create(labelMapping: LabelMapping): Logger {
        return DebugTracingLogger(
            labelMapping = labelMapping,
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
