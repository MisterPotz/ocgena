package ru.misterpotz.marking.transitions

import model.TransitionId
import ru.misterpotz.marking.objects.Time

interface TransitionTimesMarking {
    fun increaseSimTime(time: Time)
    fun earliestNonZeroTime(): Time?
    fun isAllowedToBeEnabled(transition: TransitionId): Boolean
    fun setNextAllowedTime(transition: TransitionId, time: Time)
}

fun TransitionOccurrenceAllowedTimes(transitionsToNextTimes: MutableMap<TransitionId, Time> = mutableMapOf())
        : TransitionTimesMarking {
    return TransitionTimesMarkingMap(transitionsToNextTimes)
}

internal class TransitionTimesMarkingMap(
    private val transitionsToNextTimes: MutableMap<TransitionId, Time> = mutableMapOf()
) :
    TransitionTimesMarking {

    override fun increaseSimTime(time: Time) {
        for (i in transitionsToNextTimes.keys) {
            val currentValue = transitionsToNextTimes[i]!!

            transitionsToNextTimes[i] = (currentValue - time).coerceAtLeast(0)
        }
    }

    override fun earliestNonZeroTime(): Time? {
        return transitionsToNextTimes.values.fold(null) { accum: Time?, right: Time ->
            if (right > 0 || (accum != null && accum > right)) {
                right
            } else {
                accum
            }
        }
    }
    
    override fun isAllowedToBeEnabled(transition: TransitionId): Boolean {
        return transitionsToNextTimes[transition] == 0L
    }

    override fun setNextAllowedTime(transition: TransitionId, time: Time) {
        transitionsToNextTimes[transition] = time
    }
}
