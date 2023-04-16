package error

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface Error {
    val message: String
    val errorLevel : ErrorLevel
}

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ErrorLevel {
    WARNING,
    CRITICAL
}

