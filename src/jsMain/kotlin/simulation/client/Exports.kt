package simulation.client

import error.Error
import model.OcDotParseResult

@OptIn(ExperimentalJsExport::class)
@JsExport
public external interface JsSimTaskClientCallback {
    fun onExecutionFinish()
    fun onExecutionStart()
    fun onExecutionTimeout()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
public fun toSimTaskClientCallback(jsSimTaskClientCallback: JsSimTaskClientCallback) = object  : SimTaskClientCallback {
    override fun onExecutionFinish() = jsSimTaskClientCallback.onExecutionFinish()
    override fun onExecutionStart() = jsSimTaskClientCallback.onExecutionStart()
    override fun onExecutionTimeout() = jsSimTaskClientCallback.onExecutionTimeout()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
external interface JsOnReadinessCallback {
    fun readyToCalc(boolean: Boolean)
    fun ocDotParseResult(ocDotParseResult: OcDotParseResult)
    fun onCurrentErrorsChange(errors: Array<Error>?)
}
@OptIn(ExperimentalJsExport::class)
@JsExport
public fun toOnReadinessCallback(jsOnReadinessCallback: JsOnReadinessCallback) = object  : OnReadinessCallback {
    override fun readyToCalc(boolean: Boolean) = jsOnReadinessCallback.readyToCalc(boolean)
    override fun ocDotParseResult(ocDotParseResult: OcDotParseResult) = jsOnReadinessCallback.ocDotParseResult(ocDotParseResult)
    override fun onCurrentErrorsChange(errors: Array<Error>?) = jsOnReadinessCallback.onCurrentErrorsChange(errors)
}
