package ru.misterpotz.simulation.queue

import ru.misterpotz.model.marking.Time
import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.ObjectMarkingDelta

interface GenerationQueue {
    fun shiftTime(time: Time)
    fun generateTokensAsMarkingAndReplan(): ObjectMarkingDelta?
    fun getTimeUntilNextPlanned(): Time?
    fun planTokenGenerationForEveryone()
}