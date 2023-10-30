package simulation

import model.*
import ru.misterpotz.model.marking.Time
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

    fun onStart()
    fun afterInitialMarking()

    fun onExecutionNewStepStart()

    fun beforeStartingNewTransitions()
    fun onStartTransition(transition: ActiveFiringTransition)
    fun afterStartingNewTransitions()

    fun beforeEndingTransitions()
    fun onEndTransition(executedBinding: ExecutedBinding)
    fun afterEndingTransitions()

    fun onExecutionStepFinish(newTimeDelta: Time)

    fun afterFinalMarking()

    fun onTimeout()

    fun onEnd()
}
