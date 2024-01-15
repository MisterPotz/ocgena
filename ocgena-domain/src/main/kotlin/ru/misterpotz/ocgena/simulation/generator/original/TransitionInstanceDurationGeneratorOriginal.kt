package ru.misterpotz.ocgena.simulation.generator.original

import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time
import kotlin.random.Random

class TransitionInstanceDurationGeneratorOriginal(
    private val random: Random?,
    private val transitionsOriginalSpec: TransitionsOriginalSpec,
) {
    fun newDuration(transition: PetriAtomId): Time {
        val times = transitionsOriginalSpec[transition]
        val duration = random?.let {
            times.duration.intRange.random(random)
        } ?: times.duration.intRange.first
        return duration.toLong()
    }
}