@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")
@file:JsModule("ocdot-parser")
@file:JsNonModule
external interface FilePosition {
    var offset: Number
    var line: Number
    var column: Number
}

external interface FileRange {
    var start: FilePosition
    var end: FilePosition
    var source: String
}
