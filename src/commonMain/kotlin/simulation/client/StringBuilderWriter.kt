package simulation.client

import kotlin.js.JsExport

@JsExport
class StringBuilderWriter : Writer {
    private val htmlStringBuilder = StringBuilder()

    override fun writeLine(line: String) {
        htmlStringBuilder.appendLine(line)
    }

    override fun end() {

    }

    fun collect(): String {
        return htmlStringBuilder.toString()
    }
}


@JsExport
class HtmlDebugTraceBuilderWriter(private val strinbBuilderWriter: StringBuilderWriter = StringBuilderWriter()) :
    Writer by strinbBuilderWriter {
    fun collect(): String {
        return HtmlExecutionTraceGenerator(strinbBuilderWriter.collect()).generate()
    }

}
