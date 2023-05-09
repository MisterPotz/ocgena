package simulation.client

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface SimCallback {
    fun onFinishedSimulation()
}
