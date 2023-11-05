package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.simulation.config.TransitionInstancesTimesSpec
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time
import kotlin.random.Random

class TransitionInstanceDurationGenerator(
    private val random: Random?,
    private val transitionInstancesTimesSpec: TransitionInstancesTimesSpec,
) {
    fun newDuration(transition: PetriAtomId): Time {
        val times = transitionInstancesTimesSpec[transition]
        val duration = random?.let {
            times.duration.intRange.random(random)
        } ?: times.duration.intRange.first
        return duration.toLong()
    }
}