package converter

import config.ConfigToDomainConverter
import config.SimulationConfig

class SimulationConfigStore {
    private var lastSimulationConfig: SimulationConfig? = null
    private var lastProcessingResult: ConfigProcessingResult? = null

    fun isEmpty(): Boolean {
        return lastProcessingResult == null
    }

    fun requireSimulationConfig(): SimulationConfig {
        return requireNotNull(lastSimulationConfig)
    }

    fun requireProcessingResult(): ConfigProcessingResult {
        return requireNotNull(lastProcessingResult)
    }

    fun updateSimulationConfig(simulationConfig: SimulationConfig) {
        this.lastSimulationConfig = simulationConfig
        this.lastProcessingResult = ConfigToDomainConverter(simulationConfig).processAll()
    }
}
