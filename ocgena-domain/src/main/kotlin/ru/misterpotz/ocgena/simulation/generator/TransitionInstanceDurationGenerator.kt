package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.simulation.config.IntervalFunction
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time
import kotlin.random.Random

class TransitionInstanceDurationGenerator(
    private val random: Random?,
    private val intervalFunction: IntervalFunction,
) {
    fun newDuration(transition: PetriAtomId): Time {
        val times = intervalFunction[transition]
        val duration = random?.let {
            times.duration.random(random)
        } ?: times.duration.first
        return duration.toLong()
    }
}