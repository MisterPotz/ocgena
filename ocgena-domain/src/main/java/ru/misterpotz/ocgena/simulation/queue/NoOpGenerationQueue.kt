package ru.misterpotz.ocgena.simulation.queue

import ru.misterpotz.marking.objects.Time
import ru.misterpotz.ocgena.collections.objects.ImmutableObjectMarking

class NoOpGenerationQueue() : GenerationQueue {
    override fun shiftTime(time: Time) {

    }

    override fun generateTokensAsMarkingAndReplan(): ImmutableObjectMarking? {
        return null
    }

    override fun getTimeUntilNextPlanned(): Time? {
        return null
    }

    override fun planTokenGenerationForEveryone() {

    }
}