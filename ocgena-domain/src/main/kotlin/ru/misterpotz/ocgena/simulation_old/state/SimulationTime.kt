package ru.misterpotz.ocgena.simulation_old.state

import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.utils.print

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
