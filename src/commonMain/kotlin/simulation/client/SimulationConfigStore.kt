package simulation.client

import config.processConfig
import kotlinx.coroutines.flow.MutableStateFlow
import simulation.ProcessedSimulationConfig
import simulation.config.SimulationConfig

class SimulationConfigStore() {
    val simulationConfigFlow: MutableStateFlow<ProcessedSimulationConfig?> = MutableStateFlow(null)

    fun updatePlainConfig(simulationConfig: SimulationConfig) {
        val configProcessingResult = runCatching {
            processConfig(simulationConfig)
        }
        val processedConfig = if (configProcessingResult.isSuccess) {
            configProcessingResult.getOrThrow()
        } else {
            null
        }

        simulationConfigFlow.value = processedConfig
    }
}
