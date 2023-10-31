package ru.misterpotz.simulation.transition

import ru.misterpotz.model.marking.Time
import model.TransitionId
import model.time.IntervalFunction
import javax.inject.Inject
import kotlin.random.Random

class TransitionInstanceNextCreationTimeGenerator @Inject constructor(
    private val random: Random?,
    private val intervalFunction: IntervalFunction,
) {
    fun get(interval: IntRange): Time {
        return (random?.let {
            interval.random(random = it)
        } ?: interval.first).toLong()
    }

    fun getNewActivityNextAllowedTime(transition: TransitionId): Time {
        return get(intervalFunction[transition].pauseBeforeNextOccurence)
    }
}
