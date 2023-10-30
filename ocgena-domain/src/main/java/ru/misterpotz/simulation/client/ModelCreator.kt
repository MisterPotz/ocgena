package simulation.client

import converter.FullModelBuilder
import error.Error
import error.ErrorClass
import error.ErrorLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import model.OcDotParseResult

class ModelCreator(
    private val ocDotStore: OcDotStore,
    private val simulationConfigStore: SimulationConfigStore,
) {
    class ModelBuildingRequest(
        val ocDot: String,
        val processedSimulationConfig: ProcessedSimulationConfig,
    )

    val builtModelFlow = MutableStateFlow<OcDotParseResult?>(null)
    val errorsFlow = MutableStateFlow<List<Error>?>(null)

    private fun createNotEnoughDataError() : Error {
        return ErrorClass(message = "one of the required sources is absent or not valid, status : [ocDot : ${ocDotStore.ocDotFlow.value != null}] [sim config : ${simulationConfigStore.simulationConfigFlow.value != null}]", errorLevel = ErrorLevel.CRITICAL)
    }

    private fun createCriticalErrorDuringConversion(exception: Throwable?) : Error {
        println("createCriticalErrorDuringConversion: $exception")
        return ErrorClass(message = "ModelCreator: critical exception occurred during conversion, original message : ${exception?.message}", errorLevel = ErrorLevel.CRITICAL)
    }

    fun tryCreateModelFromModelBuildingRequest(it: ModelBuildingRequest): Result<OcDotParseResult> {
        return buildModel(it)
    }

    fun createModelSafely(ocDot: String, simulationConfig: ProcessedSimulationConfig) : OcDotParseResult? {
        val modelBuildingRequest = ModelBuildingRequest(ocDot, simulationConfig)
        return tryCreateModelFromModelBuildingRequest(modelBuildingRequest).getOrNull()
    }

    suspend fun startModelCreation() {
        ocDotStore.ocDotFlow.combine(
            simulationConfigStore.simulationConfigFlow
        ) { ocDot, simulationConfig ->
            if (ocDot == null || simulationConfig == null) {
                builtModelFlow.emit(null)
                println("lacking sources")
                val notEnoughDataError = createNotEnoughDataError()
                errorsFlow.value = listOf(notEnoughDataError)

                return@combine null
            }

            ModelBuildingRequest(ocDot, simulationConfig)
        }
            .collectLatest {
                println("ModelCreator: collecting request $it")
                if (it == null) {
                    builtModelFlow.emit(null);
                    return@collectLatest;
                }
                val ocDotParseResult = tryCreateModelFromModelBuildingRequest(it)

                if (ocDotParseResult == null) {
                    builtModelFlow.emit(null);
                    return@collectLatest
                }

                if (ocDotParseResult.isSuccess) {
                    val builtModelResult = ocDotParseResult.getOrThrow()
                    builtModelFlow.emit(builtModelResult)
                    errorsFlow.value = builtModelResult.errors.toList()
                } else {
                    val criticalErorr = createCriticalErrorDuringConversion(ocDotParseResult.exceptionOrNull())
                    errorsFlow.value = listOf(criticalErorr)
                    builtModelFlow.value = null
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
