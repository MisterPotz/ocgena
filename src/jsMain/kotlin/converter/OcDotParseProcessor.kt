package converter

import model.InputOutputPlaces
import model.OcNetType
import model.PlaceTyping

class ParseProcessorParams(
    val placeTyping: PlaceTyping,
    val inputOutputPlaces: InputOutputPlaces,
    val netType : OcNetType
)

class OcDotParseProcessor(
    private val parseProcessorParams: ParseProcessorParams,
) {
    private val errorReporterContainer = DefaultErrorReporterContainer()

    val ocDotParserV2 = OcDotParserV2(errorReporterContainer)

    fun process(ocdot: String): OcDotParseResult {
        val task = ParseProcessingTask(
            parseProcessorParams,
            ocDotParserV2,
            errorReporterContainer,
            ocDot = ocdot,
        )
        return task.process()
    }
}
