package simulation.client

import error.Error
import model.OcDotParseResult
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface OnReadinessCallback {
    fun readyToCalc(boolean: Boolean)
    fun ocDotParseResult(ocDotParseResult: OcDotParseResult)
    fun onCurrentErrorsChange(errors : Array<Error>?)
}
