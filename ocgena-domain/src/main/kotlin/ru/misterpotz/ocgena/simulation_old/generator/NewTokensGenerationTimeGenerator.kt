package ru.misterpotz.ocgena.simulation_old.generator

import ru.misterpotz.ocgena.simulation_old.config.Period
import ru.misterpotz.ocgena.simulation_old.Time
import simulation.random.RandomSource
import javax.inject.Inject

class NewTokensGenerationTimeGenerator @Inject constructor(
    private val randomSource: RandomSource,
) {
    fun get(interval: Period): Time {
        return (interval.start..interval.end).random(random = randomSource.backwardSupport()).toLong()
    }
}
