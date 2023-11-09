package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.simulation.config.MarkingScheme

interface NewTokenTimeBasedGenerator {
    fun increaseTime(time: Time)
    fun generateFictiveTokensAsMarkingSchemeAndReplan() : MarkingScheme?
    fun getTimeUntilNextPlanned(): Time?
    fun planTokenGenerationForEveryone()
}