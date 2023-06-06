package simulation.client.loggers

import eventlog.EventLog
import eventlog.ModelToEventLogConverter
import model.ActiveFiringTransition
import model.ExecutedBinding
import model.LabelMapping
import simulation.SimulatableComposedOcNet
import simulation.SimulationTime
import simulation.client.OcelParams
import simulation.client.OcelWriter

class OcelEventLogger(
    private val ocelParams: OcelParams,
    loggingEnabled: Boolean,
    val labelMapping: LabelMapping,
    val ocelWriter: OcelWriter,
) : StubLogger() {

    private val eventLog = EventLog()

    private val modelToEventLogConverter = ModelToEventLogConverter(
        labelMapping = labelMapping,
        ocelParams = ocelParams
    )
    override val loggingEnabled: Boolean = loggingEnabled
    override fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime
    ) = Unit
    override fun onTransitionEnded(executedBinding: ExecutedBinding) {
        val event = modelToEventLogConverter.convertToEventEnd(executedBinding)
        eventLog.recordEvent(event)
        val objects = modelToEventLogConverter.getObjects(executedBinding)
        eventLog.recordObjects(objects)
    }

    override fun onTransitionStart(transition: ActiveFiringTransition) {
        val event = modelToEventLogConverter.convertToEventStart(transition)
        val objects = modelToEventLogConverter.getObjects(transition)
        eventLog.recordObjects(objects)
        if (event != null) {
            eventLog.recordEvent(event)
        }
    }

    override fun onEnd() {
        ocelWriter.write(eventLog)
    }
}
