package simulation.client

import config.processConfig
import error.Error
import error.ErrorClass
import error.ErrorLevel
import kotlinx.coroutines.flow.MutableStateFlow
import simulation.config.ConfigHolder

class SimulationConfigStore() {
    val simulationConfigFlow: MutableStateFlow<ProcessedSimulationConfig?> = MutableStateFlow(null)
    val simulationConfigTransformationErrors : MutableStateFlow<List<Error>?> = MutableStateFlow(null)

    fun mapSimulationConfigSafely(configHolder: ConfigHolder) : ProcessedSimulationConfig? {
        val configProcessingResult = runCatching {
            processConfig(configHolder)
        }
        val processedConfig = if (configProcessingResult.isSuccess) {
            configProcessingResult.getOrThrow()
        } else {
            null
        }
        return processedConfig
    }

    fun updatePlainConfig(configHolder: ConfigHolder) {
        val configProcessingResult = runCatching {
            processConfig(configHolder)
        }
        val processedConfig = if (configProcessingResult.isSuccess) {
            simulationConfigTransformationErrors.value = null
            configProcessingResult.getOrThrow()

        } else {
            val exception = configProcessingResult.exceptionOrNull()
            val mappedException = ErrorClass(exception?.message ?: "SimulationConfigStore: couldn't create simulation config", errorLevel = ErrorLevel.CRITICAL)
            simulationConfigTransformationErrors.value = listOf(mappedException)

            null
        }

        simulationConfigFlow.value = processedConfig
    }
}
