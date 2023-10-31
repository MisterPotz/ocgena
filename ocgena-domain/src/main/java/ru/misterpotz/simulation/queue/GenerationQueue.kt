package ru.misterpotz.simulation.queue

import ru.misterpotz.marking.objects.Time
import ru.misterpotz.marking.objects.ObjectMarkingDelta

interface GenerationQueue {
    fun shiftTime(time: Time)
    fun generateTokensAsMarkingAndReplan(): ObjectMarkingDelta?
    fun getTimeUntilNextPlanned(): Time?
    fun planTokenGenerationForEveryone()
}