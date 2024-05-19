package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.misterpotz.Logger
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput


class Simulation(
    val simulationInput: SimulationInput,
    private val stepExecutor: StepExecutor,
    private val finishRequestChecker: FinishRequestChecker,
    private val logger: Logger
) {
    suspend fun runSimulation() {
        val flow = flow {
            while (!finishRequestChecker.isFinish()) {
                val value = stepExecutor.execute()
                if (value != null) {
                    emit(value)
                } else {
                    break
                }
            }
        }
        flow.onStart {
            logger.simulationPrepared()
        }.onCompletion {
            logger.simulationFinished()
            println("simulationFinished")
        }.collect { log ->
            logger.acceptStepLog(log)
        }
    }
}