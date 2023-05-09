package config

import simulation.ProcessedSimulationConfig
import simulation.config.SimulationConfig

expect class SimulationConfigProcessor(simulationConfig: SimulationConfig) {
    fun createProcessedConfig(): ProcessedSimulationConfig
}

fun processConfig(simulationConfig: SimulationConfig) : ProcessedSimulationConfig {
    val converter = SimulationConfigProcessor(simulationConfig)
    return converter.createProcessedConfig()
}
