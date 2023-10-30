package model

import error.Error
import error.ErrorLevel
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class OcDotParseResult(
    @Suppress("NON_EXPORTABLE_TYPE")
    val ocNet: StaticCoreOcNet? = null,
    val errors: Array<Error>,
) {
    val hasCriticalErrors
        get() =  errors.find { it.errorLevel == ErrorLevel.CRITICAL } != null

}
