package parse

import dsl.OCNetFacadeBuilder
import error.ConsistencyCheckError

@OptIn(ExperimentalJsExport::class)
@JsExport
sealed class OCDotParseResult {
    data class SyntaxParseError(val message: String, val location: FileRange?) : OCDotParseResult()

    data class SemanticParseException(
        val message: String,
        val originalException: Throwable?,
    ) : OCDotParseResult()

    data class SemanticCriticalErrorsFound(val message: String, val collectedSemanticErrors: List<SemanticError>) :
        OCDotParseResult()

    data class DomainCheckCriticalErrorsFound(val message: String, val collectedSemanticErrors: List<ConsistencyCheckError>) : OCDotParseResult()
    data class Success(val buildOCNet: OCNetFacadeBuilder.BuiltOCNet) : OCDotParseResult()
}
