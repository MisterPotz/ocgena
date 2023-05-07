package converter

import config.ConfigToDomainConverter
import config.SimulationConfig
import simulation.ProcessedSimulationConfig

class SimulationConfigStore {
    private var lastSimulationConfig: SimulationConfig? = null
    private var lastProcessingResult: ProcessedSimulationConfig? = null

    fun isEmpty(): Boolean {
        return lastProcessingResult == null
    }

    fun requireSimulationConfig(): SimulationConfig {
        return requireNotNull(lastSimulationConfig)
    }

    fun requireProcessingResult(): ProcessedSimulationConfig {
        return requireNotNull(lastProcessingResult)
    }

    fun updateSimulationConfig(simulationConfig: SimulationConfig) {
        this.lastSimulationConfig = simulationConfig
        this.lastProcessingResult = ConfigToDomainConverter(simulationConfig).processAll()
    }
}
