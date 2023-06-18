package simulation

import kotlinx.serialization.Serializable
import model.Time
import model.Transition
import model.TransitionId
import utils.print

@Serializable
class SerializableTransitionOccurrenceAllowedTimes(val transitionsToNextTimes : Map<TransitionId, Time>)

class TransitionOccurrenceAllowedTimes {
    private val transitionsToNextTimes = mutableMapOf<Transition, Time>()

    fun toSerializable() : SerializableTransitionOccurrenceAllowedTimes {
        return SerializableTransitionOccurrenceAllowedTimes(
            buildMap {
                for (i in transitionsToNextTimes.keys) {
                    put(i.id, transitionsToNextTimes[i]!!)
                }
            }wo
        )
    }
    fun shiftByTime(time: Time) {
        for (i in transitionsToNextTimes.keys) {
            val currentValue = transitionsToNextTimes[i]!!

            transitionsToNextTimes[i] = (currentValue - time).coerceAtLeast(0)
        }
    }

    fun earliestNonZeroTime() : Time? {
        return transitionsToNextTimes.filter { it.value > 0 }.minByOrNull {
            it.value
        }?.value?.takeIf { it > 0 }
    }

    fun isAllowedToBeEnabled(transition: Transition) : Boolean {
        return (transitionsToNextTimes[transition] == 0)
    }

    fun setNextAllowedTime(transition: Transition, time: Time) {
        transitionsToNextTimes[transition] = time
    }

    fun prettyPrintState(): String {
        return transitionsToNextTimes.keys.joinToString(separator = "\n") {
            """${it.id} permitted in ${transitionsToNextTimes[it]?.print()}"""
        }
    }

    fun htmlLines() : List<String> {
        return transitionsToNextTimes.keys.map {
            """${it.id} permitted in ${transitionsToNextTimes[it]?.print()}"""
        }
    }
}
