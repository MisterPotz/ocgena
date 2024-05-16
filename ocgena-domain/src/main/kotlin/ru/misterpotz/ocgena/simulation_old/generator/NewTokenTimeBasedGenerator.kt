package ru.misterpotz.ocgena.simulation_old.generator

import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.simulation_old.config.MarkingScheme

interface NewTokenTimeBasedGenerator {
    fun increaseTime(time: Time)
    fun generateFictiveTokensAsMarkingSchemeAndReplan() : MarkingScheme?
    fun getTimeUntilNextPlanned(): Time?
    fun planTokenGenerationForEveryone()
}