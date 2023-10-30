package ru.misterpotz.simulation.transition

import ru.misterpotz.model.marking.Time
import model.Transition
import model.time.IntervalFunction
import kotlin.random.Random

class TransitionInstanceOccurenceDeltaSelector(
    private val random: Random?,
    private val  intervalFunction: IntervalFunction,
) {
    fun get(interval : IntRange): Time {
        return random?.let {
            interval.random(random = it)
        } ?: interval.first
    }

    fun getNewNextOccurrenceTime(transition: Transition): Time {
        return get(intervalFunction[transition].pauseBeforeNextOccurence)
    }
}
