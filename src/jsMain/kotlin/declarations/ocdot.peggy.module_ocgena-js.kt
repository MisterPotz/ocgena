@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS"
)
@file:JsModule("ocdot-parser/lib/ocdot.peggy")
@file:JsNonModule
package declarations

import ClassParts
import ParseFunction
import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*

external interface FilePosition {
    var offset: Number
    var line: Number
    var column: Number
}

external interface FileRange {
    var start: FilePosition
    var end: FilePosition
    var source: String?
}

external interface LiteralExpectation {
    var type: String /* "literal" */
    var text: String
    var ignoreCase: Boolean
}


external interface ClassExpectation {
    var type: String /* "class" */
    var parts: ClassParts
    var inverted: Boolean
    var ignoreCase: Boolean
}

external interface AnyExpectation {
    var type: String /* "any" */
}

external interface EndExpectation {
    var type: String /* "end" */
}

external interface OtherExpectation {
    var type: String /* "other" */
    var description: String
}


external interface ParseOptions {
    var filename: String?
        get() = definedExternally
        set(value) = definedExternally
    var startRule: String?
        get() = definedExternally
        set(value) = definedExternally
    var tracer: Any?
        get() = definedExternally
        set(value) = definedExternally

    @nativeGetter
    operator fun get(key: String): Any?

    @nativeSetter
    operator fun set(key: String, value: Any)
}

external class PeggySyntaxError {
    val message: String
    val expected: dynamic
    val found: String?
    val location: FileRange
    val name: String
    fun format(
        sources: dynamic,
        /*sources: {
        grammarSource?: string;
        text: string;
    }[]*/
    )
}

external var parse: ParseFunction
