package converter

import converter.visitors.DelegateOCDotASTVisitorBFS
import converter.visitors.ElementsDeclarationsASTVisitorBFS
import converter.visitors.StructureCheckASTVisitorBFS
import model.InputOutputPlaces
import model.PlaceTyping

class ParseProcessingTask(
    private val placeTyping: PlaceTyping,
    private val inputOutputPlaces: InputOutputPlaces,
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
            structureContainer,
            delegateOCDotASTVisitorBFS,
            errorReporterContainer
        )
        if (!semanticChecker.checkErrors(parsingResult.getOrThrow())) {
            return createErrorResult()
        }

        val ocDotToDomainConverter = OCDotToDomainConverter(
            placeTyping,
            inputOutputPlaces,
            structureContainer
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
