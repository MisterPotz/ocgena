package eventlog

import model.*
import ru.misterpotz.model.marking.ObjectToken
import simulation.client.OcelParams
import simulation.client.loggers.TimeStampMapper

class ModelToEventLogConverter(
    private val labelMapping: LabelMapping,
    private val ocelParams: OcelParams,
    private val timeStampMapper: TimeStampMapper
) {
    private val objects: MutableList<ObjectToken> = mutableListOf()
    private val logBothStartAndEnd : Boolean = ocelParams.logBothStartAndEnd
    private val occurrenceRegistry = ActivityOccurrenceRegistry(ocelParams)

    fun getObjects(activeTransition: ActiveFiringTransition) : Set<ObjectToken> {
        return  buildSet {
            addAll(activeTransition.lockedObjectTokens.allTokens())
        }
    }

    fun getObjects(executedBinding: ExecutedBinding) : Set<ObjectToken> {
        return buildSet {
            addAll(executedBinding.producedMap.allTokens())
        }
    }

    fun convertToEventStart(activeTransition: ActiveFiringTransition) : Event? {
        if (logBothStartAndEnd) {
            val transition = activeTransition.transition

            return Event(
                eventId = idOfActivityStart(transition),
                activity = nameOfActivityStart(transition),
                timestamp = timeStampMapper.mapTime(activeTransition.startedAt),
                oMap = activeTransition.lockedObjectTokens.allTokens().map { it.name },
                vMap = mutableMapOf()
            )
        }
        return null
    }

    fun convertToEventEnd(executedBinding: ExecutedBinding): Event {
        val transition = executedBinding.finishedTransition.transition
        return Event(
            activity = nameOfActivityEnd(transition),
            timestamp = timeStampMapper.mapTime(executedBinding.finishedTime),
            oMap = executedBinding.producedMap.allTokens().map { it.name },
            eventId = idOfActivityEnd(transition),
            vMap = mutableMapOf()
        )
    }


    private fun nameOfActivityStart(transition: Transition) : String {
        return labelMapping[transition] + "_Start"
    }

    private fun nameOfActivityEnd(transition: Transition) : String {

        return labelMapping[transition] + if (logBothStartAndEnd) "_End" else ""
    }

    private fun idOfActivityStart(transition: Transition) : String {
        val occerrences = occurrenceRegistry.onStartOccurrencesOf(transition.id)

        return transition.id + "_$occerrences" + "_Start"
    }

    private fun idOfActivityEnd(transition: Transition) : String {
        val occerrences = occurrenceRegistry.onEndOccurrencesOf(transition.id)
        return transition.id + "_$occerrences" + if (logBothStartAndEnd) "_End" else ""
    }
}