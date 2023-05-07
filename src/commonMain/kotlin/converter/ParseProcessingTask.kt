package converter

import model.OcDotParseResult

class ParseProcessingTask(
    private val parseProcessorParams: ParseProcessorParams,
    private val ocDotParserV2: OcDotParserV2,
    private val errorReporterContainer: ErrorReporterContainer,
    private val ocDot: String,
) {
    private val structureContainer = StructureContainer()
    private val semanticCheckerCreator = SemanticCheckerCreator(
        structureContainer, errorReporterContainer
    )
    fun process(): OcDotParseResult {
        val parsingResult = ocDotParserV2.parse(ocDot)
        if (parsingResult.isFailure) {
            return createErrorResult()
        }
        val semanticChecker = semanticCheckerCreator.create()
        if (!semanticChecker.checkErrors(parsingResult.getOrThrow())) {
            return createErrorResult()
        }

        val ocDotToDomainConverter = OCDotToDomainConverter(
            ConversionParams(
                placeTyping = parseProcessorParams.placeTyping,
                inputOutputPlaces = parseProcessorParams.inputOutputPlaces,
                dslElementsContainer = structureContainer,
                useType = parseProcessorParams.netType
            )
        )
        val builtOcNet = ocDotToDomainConverter.convert()
        builtOcNet.errors.forEach {
            errorReporterContainer.pushError(it)
        }
        return OcDotParseResult(
            ocNet = builtOcNet.ocNet,
            errors = errorReporterContainer.collectReport().toTypedArray()
        )
    }

    private fun createErrorResult() : OcDotParseResult {
        return OcDotParseResult(null, errorReporterContainer.collectReport().toTypedArray())
    }
}
