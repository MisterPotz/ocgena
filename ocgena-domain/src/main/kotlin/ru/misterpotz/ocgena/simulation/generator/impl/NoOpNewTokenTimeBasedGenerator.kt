package ru.misterpotz.ocgena.simulation.generator.impl

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerator

class NoOpNewTokenTimeBasedGenerator() : NewTokenTimeBasedGenerator {
    override fun increaseTime(time: Time) {

    }

    override fun generateFictiveTokensAsMarkingSchemeAndReplan(): MarkingScheme? {
        return null
    }

    override fun getTimeUntilNextPlanned(): Time? {
        return null
    }

    override fun planTokenGenerationForEveryone() {

    }
}