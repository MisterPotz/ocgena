package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.simulation.config.IntervalFunction
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time
import javax.inject.Inject
import kotlin.random.Random

class TransitionNextInstanceAllowedTimeGenerator @Inject constructor(
    private val random: Random?,
    private val intervalFunction: IntervalFunction,
) {
    fun get(interval: IntRange): Time {
        return (random?.let {
            interval.random(random = it)
        } ?: interval.first).toLong()
    }

    fun getNewActivityNextAllowedTime(transition: PetriAtomId): Time {
        return get(intervalFunction[transition].pauseBeforeNextOccurence)
    }
}
