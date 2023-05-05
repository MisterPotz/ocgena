package converter

import config.ConfigToDomainConverter
import config.SimulationConfig

class FullModelBuildingTask(
    val ocDot : String,
    val processedConfig : ConfigProcessingResult
) {
    fun process() : OcDotParseResult {
        val ocdot = ocDot
        val ocDotParseProcessor = OcDotParseProcessor(processedConfig.placeTyping, processedConfig.inputOutputPlaces)
        val processingResult = ocDotParseProcessor.process(ocdot)

        return processingResult
    }
}

class FullModelBuilder {
    private var simulationConfig : SimulationConfig? = null
    private var ocDot: String? = null

    fun with(simulationConfig: SimulationConfig) {
        this.simulationConfig = simulationConfig
    }

    fun with(ocdot: String) {
        this.ocDot = ocdot
    }

    fun tryBuildTask() : FullModelBuildingTask? {
        val simulationConfig = ConfigToDomainConverter(simulationConfig = simulationConfig ?: return null).processAll()

        return FullModelBuildingTask(
             ocDot = ocDot ?: return null,
            processedConfig = simulationConfig
        )
    }
}
