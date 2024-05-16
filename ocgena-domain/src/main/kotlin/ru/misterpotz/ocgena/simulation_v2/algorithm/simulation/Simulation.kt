package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.misterpotz.Logger
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenStore
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput


class Simulation(
    val simulationInput: SimulationInput,
    private val modelAccessor: ModelAccessor,
    private val tokenStore: TokenStore,
    private val stepExecutor: StepExecutor,
    private val logger: Logger
) {

    fun prepare() {
        simulationInput.places.forEach { (place, setting) ->
            if (setting.initialTokens != null) {
                tokenStore.setAmount(modelAccessor.place(place), setting.initialTokens)
            }
        }
    }

    suspend fun runSimulation() {
        val flow = flow {
            while (true) {
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
        }.collect { log ->
            logger.acceptStepLog(log)
        }
    }
}