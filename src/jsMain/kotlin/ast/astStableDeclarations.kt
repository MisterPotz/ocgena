@file:JsModule("ocdot-parser/lib/ast")
@file:JsNonModule
@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS"
)
@file:JsQualifier("AST") // because AST is exported as namespace
package ast

import declarations.FileRange

external class PeggySyntaxError {
    val message: String;
    val expected: Array<dynamic /* Expectation */ >;
    val found: String?;
    val location: FileRange;
    val name: String

}
