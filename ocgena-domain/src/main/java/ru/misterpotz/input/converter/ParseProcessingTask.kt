package converter

import model.OcDotParseResult

class ParseProcessingTask(
    private val parseProcessorParams: ParseProcessorParams,
    private val ocDotParserV2: OcDotParserV2,
    private val errorReporterContainer: ErrorReporterContainer,
    private val ocDot: String,
) {
    private val destStructureContainer = StructureContainer()
    private val semanticVisitorCheckerCreator = SemanticCheckerCreator(
        destStructureContainer, errorReporterContainer
    )
    fun process(): OcDotParseResult {
        val parsingResult = ocDotParserV2.parse(ocDot)
        if (parsingResult.isFailure) {
            return createErrorResult()
        }
        val semanticVisitorChecker = semanticVisitorCheckerCreator.create()
        if (!semanticVisitorChecker.checkErrors(parsingResult.getOrThrow())) {
            return createErrorResult()
        }

        val ocDotToDomainConverter = OCDotToDomainConverter(
            ConversionParams(
                placeTyping = parseProcessorParams.placeTyping,
                inputOutputPlaces = parseProcessorParams.inputOutputPlaces,
                dslElementsContainer = destStructureContainer,
                useType = parseProcessorParams.netType
            )
        )
        val builtOcNet = ocDotToDomainConverter.convert()
        println("got builtOcNet, success: ${builtOcNet.ocNet != null}")
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
