package config

import simulation.config.ConfigHolder

interface SimulationConfigProcessor {
    fun createProcessedConfig(configHolderProcessor: ConfigHolder): ProcessedSimulationConfig
}

fun processConfig(
    configHolder: ConfigHolder,
    simulationConfigProcessorProvider : Function<SimulationConfigProcessor>
): ProcessedSimulationConfig {
    val simulationConfigProcessor = simulationConfigProcessorProvider.invoke()
    return simulationConfigProcessorProvider()
    return converter.createProcessedConfig()
}
