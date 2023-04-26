package eventlog

import model.ExecutedBinding
import model.LabelsActivities
import utils.print

class ModelToEventLogConverter(
    private val labelsActivities: LabelsActivities
) {
    fun executedToEvent(executedBinding: ExecutedBinding): Event {
        return Event(
            activity = labelsActivities[executedBinding.finishedTransition.transition],
            timestamp = executedBinding.finishedTime.print(),
            oMap = executedBinding.producedMap.allTokens().map { it.name },
            vMap = mutableMapOf()
        )
    }
}
