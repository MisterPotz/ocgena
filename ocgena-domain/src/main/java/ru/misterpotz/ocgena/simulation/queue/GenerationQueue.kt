package ru.misterpotz.ocgena.simulation.queue

import ru.misterpotz.ocgena.collections.objects.ObjectMarkingDelta
import ru.misterpotz.ocgena.simulation.Time

interface GenerationQueue {
    fun shiftTime(time: Time)
    fun generateTokensAsMarkingAndReplan(): ObjectMarkingDelta?
    fun getTimeUntilNextPlanned(): Time?
    fun planTokenGenerationForEveryone()
}