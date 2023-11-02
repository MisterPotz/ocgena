package simulation.client

import kotlin.js.JsExport

@JsExport
interface Writer {
    fun writeLine(line: String)
    fun end()
}
