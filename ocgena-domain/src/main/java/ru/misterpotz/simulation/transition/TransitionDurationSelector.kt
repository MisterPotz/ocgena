package ru.misterpotz.simulation.transition

import ru.misterpotz.model.marking.Time
import model.Transition
import model.time.IntervalFunction
import kotlin.random.Random

class TransitionDurationSelector(
    private val random: Random?,
    private val intervalFunction: IntervalFunction,
) {
    fun newDuration(transition: Transition): Time {
        val times = intervalFunction[transition]
        val duration = random?.let {
            times.duration.random(random)
        } ?: times.duration.first
        return duration
    }
}