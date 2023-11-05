package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingDelta
import ru.misterpotz.ocgena.simulation.Time

interface NewTokenTimeBasedGenerator {
    fun increaseTime(time: Time)
    fun generateTokensAsMarkingAndReplan(): PlaceToObjectMarkingDelta?
    fun getTimeUntilNextPlanned(): Time?
    fun planTokenGenerationForEveryone()
}