package ru.misterpotz.simulation

import ru.misterpotz.Logger
import ru.misterpotz.SimulationStepLog

object NoOpLogger : Logger {
    override suspend fun simulationPrepared() {
    }

    override suspend fun acceptStepLog(simulationStepLog: SimulationStepLog) {
    }

    override suspend fun simulationFinished() {
    }
}