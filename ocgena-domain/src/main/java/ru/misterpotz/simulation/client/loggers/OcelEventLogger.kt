package simulation.client.loggers

import eventlog.EventLog
import eventlog.ModelToEventLogConverter
import model.ActiveFiringTransition
import model.ExecutedBinding
import model.LabelMapping
import model.Time
import model.time.formatMillisToUTCString
import model.time.getSmartCurrentTime
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.state.SimulationTime
import simulation.client.OcelParams
import simulation.client.OcelWriter
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TimeStampMapper(private val baseTimeMillis: Long = getSmartCurrentTime()) {
    fun mapTime(time: Time) : String {
        val resultTime = baseTimeMillis.toDuration(DurationUnit.MILLISECONDS) + (time.toDuration(DurationUnit.MINUTES))
        return formatMillisToUTCString(resultTime)
    }
}

class OcelEventLogger(
    private val ocelParams: OcelParams,
    loggingEnabled: Boolean,
    val labelMapping: LabelMapping,
    val ocelWriter: OcelWriter,
    private val timmeStampMapper: TimeStampMapper = TimeStampMapper()
) : NoOpLogger() {

    private val eventLog = EventLog()

    private val modelToEventLogConverter = ModelToEventLogConverter(
        labelMapping = labelMapping,
        ocelParams = ocelParams,
        timeStampMapper = timmeStampMapper,
    )
    override val loggingEnabled: Boolean = loggingEnabled
    override fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime
    ) = Unit
    override fun onEndTransition(executedBinding: ExecutedBinding) {
        val event = modelToEventLogConverter.convertToEventEnd(executedBinding)
        eventLog.recordEvent(event)
        val objects = modelToEventLogConverter.getObjects(executedBinding)
        eventLog.recordObjects(objects)
    }

    override fun onStartTransition(transition: ActiveFiringTransition) {
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
