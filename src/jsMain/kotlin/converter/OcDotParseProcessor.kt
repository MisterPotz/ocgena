package converter

import model.InputOutputPlaces
import model.PlaceTyping

class OcDotParseProcessor(
    private val placeTyping: PlaceTyping,
    private val inputOutputPlaces: InputOutputPlaces
) {
    private val errorReporterContainer = DefaultErrorReporterContainer()

    val ocDotParserV2 = OcDotParserV2(errorReporterContainer)

    fun process(ocdot: String): OcDotParseResult {
        val task = ParseProcessingTask(
            placeTyping,
            inputOutputPlaces,
            ocDotParserV2,
            errorReporterContainer,
            ocDot = ocdot,
        )
        return task.process()
    }
}
