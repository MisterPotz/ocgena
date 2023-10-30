package ru.misterpotz.simulation.state

import ru.misterpotz.model.marking.Time
import utils.print

class SimulationTime(globalTime : Time = 0L) {
    var globalTime = globalTime
        private set

    fun shiftByDelta(delta : Time) {
        globalTime += delta
    }

    override fun toString(): String {
        return globalTime.print()
    }
}
