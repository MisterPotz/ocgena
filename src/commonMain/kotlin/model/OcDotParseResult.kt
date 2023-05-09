package model

import error.Error
import error.ErrorLevel

class OcDotParseResult(
    val ocNet: StaticCoreOcNet? = null,
    val errors: Array<Error>,
) {
    val hasCriticalErrors
        get() =  errors.find { it.errorLevel == ErrorLevel.CRITICAL } != null

}
