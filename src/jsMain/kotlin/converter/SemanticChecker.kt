package converter

import ast.OcDot
import converter.visitors.DelegateOCDotASTVisitorBFS
import error.Error
import error.ErrorLevel
import kotlinx.js.Object

class SemanticChecker (
    private val dslElementsContainer: StructureContainer,
    private val delegateOCDotASTVisitorBFS: DelegateOCDotASTVisitorBFS,
    private val errorReporterContainer: ErrorReporterContainer,
) {
    private fun doSemanticASTParse(parsedStructure: Object): Result<Unit> {
        return kotlin.runCatching {
            delegateOCDotASTVisitorBFS.visitOCDot(parsedStructure as OcDot)
        }
    }

    fun checkErrors(input: Object): Boolean {
        val parseResult = doSemanticASTParse(input)

        val errors = errorReporterContainer.collectReport()
        if (hasCriticalErrors(errors)) {
            return false
        }

        return if (parseResult.isSuccess) {
            true
        } else {
            val error = UnknownError(
                parseResult.exceptionOrNull()?.message ?: "unknown error",
                errorLevel = ErrorLevel.CRITICAL
            )
            errorReporterContainer.pushError(error)
            false
        }
    }

    private fun hasCriticalErrors(errors: List<Error>): Boolean {
        return errors.find { it.errorLevel == ErrorLevel.CRITICAL } != null
    }
}
