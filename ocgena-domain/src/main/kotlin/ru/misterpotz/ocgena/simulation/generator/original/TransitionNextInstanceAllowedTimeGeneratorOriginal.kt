package ru.misterpotz.ocgena.simulation.generator.original

import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time
import kotlin.random.Random

class TransitionNextInstanceAllowedTimeGeneratorOriginal(
    private val random: Random?,
    private val transitionsOriginalSpec: TransitionsOriginalSpec,
) {
    private fun randomValue(interval: IntRange): Long {
        return (random?.let {
            interval.random(random = it)
        } ?: interval.first).toLong()

    }

    fun get(interval: IntRange): Time {
        return randomValue(interval)
    }

    fun getNewActivityNextAllowedTime(transition: PetriAtomId): Time {
        return get(transitionsOriginalSpec[transition].timeUntilNextInstanceIsAllowed.intRange)
    }
}
