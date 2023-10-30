package ru.misterpotz.simulation.state

import model.Time
import utils.print

class SimulationTime(globalTime : Time = 0) {
    var globalTime = globalTime
        private set

    fun shiftByDelta(delta : Time) {
        globalTime += delta
    }

    override fun toString(): String {
        return globalTime.print()
    }
}
