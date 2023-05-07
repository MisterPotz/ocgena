package converter

import model.OcDotParseResult
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
