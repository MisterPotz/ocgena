package simulation.client

@JsExport
public external interface JsSimTaskClientCallback {
    fun onExecutionFinish()
    fun onExecutionStart()
    fun onExecutionTimeout()
}

@JsExport
public fun toSimTaskClientCallback(jsSimTaskClientCallback: JsSimTaskClientCallback) = object  : SimTaskClientCallback {
    override fun onExecutionFinish() = jsSimTaskClientCallback.onExecutionFinish()
    override fun onExecutionStart() = jsSimTaskClientCallback.onExecutionStart()
    override fun onExecutionTimeout() = jsSimTaskClientCallback.onExecutionTimeout()
}

@JsExport
external interface JsOnReadinessCallback {
    fun readyToCalc(boolean: Boolean)
}
@JsExport
public fun toOnReadinessCallback(jsOnReadinessCallback: JsOnReadinessCallback) = object  : OnReadinessCallback {
    override fun readyToCalc(boolean: Boolean) = jsOnReadinessCallback.readyToCalc(boolean)
}
