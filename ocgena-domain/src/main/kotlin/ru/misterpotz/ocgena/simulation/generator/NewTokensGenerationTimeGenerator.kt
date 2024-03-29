package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.simulation.config.Period
import ru.misterpotz.ocgena.simulation.Time
import javax.inject.Inject
import kotlin.random.Random

class NewTokensGenerationTimeGenerator @Inject constructor(
    private val random: Random?,
) {
    fun get(interval: Period): Time {
        if (random == null) return interval.start.toLong()
        return (interval.start..interval.end).random(random = random).toLong()
    }
}
