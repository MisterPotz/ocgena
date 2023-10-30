package ru.misterpotz.simulation.queue

import ru.misterpotz.model.marking.Time
import ru.misterpotz.model.marking.ImmutableObjectMarking

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