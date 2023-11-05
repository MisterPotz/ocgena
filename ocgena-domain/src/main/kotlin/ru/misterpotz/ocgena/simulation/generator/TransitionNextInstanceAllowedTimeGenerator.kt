package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.simulation.config.TransitionInstancesTimesSpec
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time
import javax.inject.Inject
import kotlin.random.Random

class TransitionNextInstanceAllowedTimeGenerator @Inject constructor(
    private val random: Random?,
    private val transitionInstancesTimesSpec: TransitionInstancesTimesSpec,
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
        return get(transitionInstancesTimesSpec[transition].timeUntilNextInstanceIsAllowed.intRange)
    }
}
