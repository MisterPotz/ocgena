package ru.misterpotz.ocgena.simulation_old.generator.impl

import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.simulation_old.config.MarkingScheme
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenTimeBasedGenerator

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