package simulation

import model.Time
import utils.print

class SimulationTime(var globalTime : Time = 0) {
    fun shiftByDelta(delta : Time) {
        globalTime += delta
    }

    override fun toString(): String {
        return globalTime.print()
    }
}
