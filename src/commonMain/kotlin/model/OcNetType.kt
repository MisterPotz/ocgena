package model

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class OcNetType(val source : String) {
    AALST("aalst"),
    LOMAZOVA("lomazova")
}
