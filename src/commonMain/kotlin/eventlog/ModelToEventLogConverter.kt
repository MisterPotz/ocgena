package eventlog

import model.ExecutedBinding
import model.LabelMapping
import utils.print

class ModelToEventLogConverter(
    private val labelMapping: LabelMapping
) {
    fun executedToEvent(executedBinding: ExecutedBinding): Event {
        return Event(
            activity = labelMapping[executedBinding.finishedTransition.transition],
            timestamp = executedBinding.finishedTime.print(),
            oMap = executedBinding.producedMap.allTokens().map { it.name },
            vMap = mutableMapOf()
        )
    }
}
