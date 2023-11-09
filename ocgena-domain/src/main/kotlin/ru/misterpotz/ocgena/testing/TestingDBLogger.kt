package ru.misterpotz.ocgena.testing

import ru.misterpotz.Logger
import ru.misterpotz.SimulationStepLog
import ru.misterpotz.ocgena.utils.LOG

class TestingDBLogger : Logger {
    val stepLogs: MutableList<SimulationStepLog> = mutableListOf()
    override suspend fun simulationPrepared() {
    }

    override suspend fun acceptStepLog(simulationStepLog: SimulationStepLog) {
        simulationStepLog.LOG { simulationStepLog -> "step ${simulationStepLog.stepNumber}" }
        stepLogs.add(simulationStepLog)
    }

    override suspend fun simulationFinished() {
    }
}