package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarkingDelta
import ru.misterpotz.ocgena.simulation.Time

interface NewTokenTimeBasedGenerator {
    fun increaseTime(time: Time)
    fun generateTokensAsMarkingAndReplan(): ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarkingDelta?
    fun getTimeUntilNextPlanned(): Time?
    fun planTokenGenerationForEveryone()
}