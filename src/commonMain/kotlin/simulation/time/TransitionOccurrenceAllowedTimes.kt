package simulation.time

import model.Time
import model.Transition
import utils.print

class TransitionOccurrenceAllowedTimes {
    private val transitionsToNextTimes = mutableMapOf<Transition, Time>()

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
}
