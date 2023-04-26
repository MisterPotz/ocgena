package converter

import ast.ParseOption__0
import ast.Types
import declarations.ocdot.PeggySyntaxError
import dsl.OCScopeImplCreator
import error.ErrorLevel
import kotlinx.js.Object
import kotlinx.js.jso

class OcDotParserV2(
    private val errorReporterContainer: ErrorReporterContainer,
) {
    private val ocScopeImpl = OCScopeImplCreator().createRootOCScope()


    private fun tryParse(ocDot: String): Result<Object> {
        val rule = jso<ParseOption__0> {
            rule = Types.OcDot
        }
        // always start parce from the root
        return kotlin.runCatching {
            ast.parse(ocDot, rule)
        }
    }

    fun parse(ocDot: String): Result<Object> {
        val parseResult = tryParse(ocDot)

        val error = if (parseResult.isFailure) {
            val exceptionOrNull = parseResult.exceptionOrNull()
            val peggySyntaxError = (exceptionOrNull as? PeggySyntaxError)
            if (peggySyntaxError != null) {
                PeggySyntaxErrorWrapper(peggySyntaxError)
            } else {
                UnknownError("unknown parsing error: ${exceptionOrNull?.message}", ErrorLevel.CRITICAL)
            }
        } else {
            null
        }

        if (error != null) {
            errorReporterContainer.pushError(error)
        }

        return parseResult
    }
}
