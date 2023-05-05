package converter

import converter.visitors.DelegateOCDotASTVisitorBFS
import converter.visitors.ElementsDeclarationsASTVisitorBFS
import converter.visitors.StructureCheckASTVisitorBFS

class ParseProcessingTask(
    private val parseProcessorParams: ParseProcessorParams,
    private val ocDotParserV2: OcDotParserV2,
    private val errorReporterContainer: ErrorReporterContainer,
    private val ocDot: String,
) {
    private val structureContainer = StructureContainer()

    private val structureCheckerVisitor = StructureCheckASTVisitorBFS(errorReporterContainer)
    private val elementsDeclarationsVisitor =
        ElementsDeclarationsASTVisitorBFS(structureContainer, errorReporterContainer)

    private val delegateOCDotASTVisitorBFS = DelegateOCDotASTVisitorBFS(
        listOf(
            structureCheckerVisitor,
            elementsDeclarationsVisitor,
        )
    )

    fun process(): OcDotParseResult {
        val parsingResult = ocDotParserV2.parse(ocDot)
        if (parsingResult.isFailure) {
            return createErrorResult()
        }
        val semanticChecker = SemanticChecker(
            delegateOCDotASTVisitorBFS,
            errorReporterContainer
        )
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
