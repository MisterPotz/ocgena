package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.simulation.config.Period
import ru.misterpotz.ocgena.simulation.Time
import simulation.random.RandomSource
import javax.inject.Inject
import kotlin.random.Random

class NewTokensGenerationTimeGenerator @Inject constructor(
    private val randomSource: RandomSource,
) {
    fun get(interval: Period): Time {
        return (interval.start..interval.end).random(random = randomSource.backwardSupport()).toLong()
    }
}
