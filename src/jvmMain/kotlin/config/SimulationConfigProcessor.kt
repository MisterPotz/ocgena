package config

import simulation.ProcessedSimulationConfig
import simulation.config.SimulationConfig

actual class SimulationConfigProcessor actual constructor(private val simulationConfig: SimulationConfig) {
    actual fun createProcessedConfig(): ProcessedSimulationConfig {
        TODO("Not yet implemented")
    }
}
