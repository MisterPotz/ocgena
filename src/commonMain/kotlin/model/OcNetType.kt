package model

import kotlin.js.JsExport


@JsExport
enum class OcNetType(val source : String) {
    TYPE_A("aalst"),
    TYPE_L("lomazova")
}
