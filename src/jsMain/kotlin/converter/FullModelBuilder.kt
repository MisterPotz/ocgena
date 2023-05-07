package converter

import simulation.ProcessedSimulationConfig

class FullModelBuildingTask(
    val ocDot : String,
    val processedConfig : ProcessedSimulationConfig
) {
    fun process(): OcDotParseResult {
        val ocdot = ocDot
        val ocDotParseProcessor = OcDotParseProcessor(
            parseProcessorParams = ParseProcessorParams(
                placeTyping = processedConfig.placeTyping,
                inputOutputPlaces = processedConfig.inputOutputPlaces,
                netType = processedConfig.type
            )
        )

        return ocDotParseProcessor.process(ocdot)
    }
}

class FullModelBuilder {
    private var simulationConfig : ProcessedSimulationConfig? = null
    private var ocDot: String? = null

    fun with(simulationConfig: ProcessedSimulationConfig) {
        this.simulationConfig = simulationConfig
    }

    fun with(ocdot: String) {
        this.ocDot = ocdot
    }

    fun newTask() : FullModelBuildingTask {
        return FullModelBuildingTask(
             ocDot = ocDot!!,
            processedConfig = simulationConfig!!
        )
    }
}
