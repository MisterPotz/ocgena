package converter

import model.OcDotParseResult

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
