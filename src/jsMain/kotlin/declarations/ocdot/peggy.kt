@file:JsModule("ocdot-parser/lib/ocdot.peggy")
@file:JsNonModule
package declarations.ocdot

import ast.FileRange

external interface Expectation

external class PeggySyntaxError {
    val message: String;
    val expected: Array<Expectation>;
    val found: String?;
    val location: FileRange;
    val name: String;

    companion object {
        fun buildMessage(expected: Array<Expectation>, found: String?): String
    }
}
