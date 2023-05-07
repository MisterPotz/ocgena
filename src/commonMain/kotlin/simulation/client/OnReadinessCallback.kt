package simulation.client

import kotlin.js.JsExport

@JsExport
interface OnReadinessCallback {
    fun readyToCalc(boolean: Boolean)
}
