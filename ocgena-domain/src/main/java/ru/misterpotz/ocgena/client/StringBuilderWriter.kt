package simulation.client

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
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

@OptIn(ExperimentalJsExport::class)
@JsExport
class CallbackStringWriter(
    private val lineWriter: (String) -> Unit,
) : Writer {
    override fun writeLine(line: String) {
        lineWriter.invoke(line)
    }

    override fun end() {}
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class HtmlDebugTraceBuilderWriter(private val strinbBuilderWriter: StringBuilderWriter = StringBuilderWriter()) :
    Writer by strinbBuilderWriter {
    fun collect(): String {
        return HtmlExecutionTraceGenerator(strinbBuilderWriter.collect()).generate()
    }
}


@OptIn(ExperimentalJsExport::class)
@JsExport
class HtmlDebugTraceCallbackBuilderWriter(
    private val immediateWriter: (String) -> Unit,
    private val onEndWriter : (String) -> Unit,
    private val limit : Int,
    private val strinbBuilderWriter: StringBuilderWriter = StringBuilderWriter()
) :
    Writer {
    var counter  = 0
    fun collect(): String {
        return strinbBuilderWriter.collect()
    }

    override fun writeLine(line: String) {
        if (counter > limit) return
        counter++
        immediateWriter.invoke(line)
        strinbBuilderWriter.writeLine(line)
    }

    override fun end() {
        onEndWriter(collect())
    }
}
