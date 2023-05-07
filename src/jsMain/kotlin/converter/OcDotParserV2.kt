package converter

import ast.ParseOption__0
import ast.Types
import declarations.ocdot.PeggySyntaxError
import error.ErrorLevel
import kotlinx.js.Object
import kotlinx.js.jso

class ObjectHolderJS(
    val obj: Object
) : ObjectHolder

actual class OcDotParserV2 actual constructor(
    private val errorReporterContainer: ErrorReporterContainer,
) {
    private fun tryParse(ocDot: String): Result<ObjectHolderJS> {
        val rule = jso<ParseOption__0> {
            rule = Types.OcDot
        }
        // always start parce from the root
        return kotlin.runCatching {
            ObjectHolderJS(
                ast.parse(ocDot, rule)
            )
        }
    }

    actual fun parse(ocDot: String): Result<ObjectHolder> {
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
