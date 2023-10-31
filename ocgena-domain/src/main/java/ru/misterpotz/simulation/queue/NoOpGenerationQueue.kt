package ru.misterpotz.simulation.queue

import ru.misterpotz.marking.objects.Time
import ru.misterpotz.marking.objects.ImmutableObjectMarking

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