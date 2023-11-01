package ru.misterpotz.simulation.transition

import ru.misterpotz.marking.objects.Time
import ru.misterpotz.model.atoms.TransitionId
import model.time.IntervalFunction
import kotlin.random.Random

class TransitionInstanceDurationGenerator(
    private val random: Random?,
    private val intervalFunction: IntervalFunction,
) {
    fun newDuration(transition: TransitionId): Time {
        val times = intervalFunction[transition]
        val duration = random?.let {
            times.duration.random(random)
        } ?: times.duration.first
        return duration.toLong()
    }
}