package simulation.client

import converter.FullModelBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import model.OcDotParseResult
import simulation.ProcessedSimulationConfig

class ModelCreator(
    private val ocDotStore: OcDotStore,
    private val simulationConfigStore: SimulationConfigStore,
) {
    class ModelBuildingRequest(
        val ocDot: String,
        val processedSimulationConfig: ProcessedSimulationConfig,
    )

    val builtModelFlow = MutableStateFlow<OcDotParseResult?>(null)

    suspend fun startObserving() {
        ocDotStore.ocDotFlow.combine(
            simulationConfigStore.simulationConfigFlow
        ) { ocDot, simulationConfig ->
            if (ocDot == null || simulationConfig == null) {
                builtModelFlow.emit(null)
                return@combine null
            }

            ModelBuildingRequest(ocDot, simulationConfig)
        }
            .collectLatest {
                if (it == null) {
                    builtModelFlow.emit(null)
                    return@collectLatest
                }
                val builtModel = buildModel(it)
                if (builtModel.isSuccess) {
                    builtModelFlow.emit(builtModel.getOrThrow())
                }
            }
    }

    private fun buildModel(modelBuildingRequest: ModelBuildingRequest): Result<OcDotParseResult> {
        val fullModelBuilder = FullModelBuilder().apply {
            withOcDot(modelBuildingRequest.ocDot)
            withConfig(modelBuildingRequest.processedSimulationConfig)
        }
        val modelBuildingTask = fullModelBuilder.newTask()

        val modelBuildingResult = runCatching {
            modelBuildingTask.process()
        }
        return modelBuildingResult
    }
}
