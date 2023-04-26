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

import ClassParts
import Kind
import Kind1
import Readonly
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
    var source: String
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

external interface `T$0` {
    var OcDot: String /* "ocdot" */
    var Comment: String /* "comment" */
    var Ocnet: String /* "ocnet" */
    var Attribute: String /* "attribute" */
    var Attributes: String /* "attributes" */
    var Edge: String /* "edge" */
    var Node: String /* "node" */
    var NodeRef: String /* "node_ref" */
    var Subgraph: String /* "subgraph" */
    var EdgeSubgraph: String /* "edge_subgraph" */
    var Literal: String /* "literal" */
    var ClusterStatements: String /* "cluster_statements" */
    var TypeDefinitions: String /* "type_definitions" */
}


external interface `T$1` {
    var Places: String /* "places" */
    var Transitions: String /* "transitions" */
}

external var SubgraphSpecialTypes: `T$1`

external interface EdgeSubgraph : ASTBaseParent<dynamic /* Attribute | Attributes | Edge | Node | Subgraph | Comment */> {
    var id: Literal__0?
        get() = definedExternally
        set(value) = definedExternally
}

external interface `T$2` {
    var Normal : String  /* "->" */
    var Variable : String  /* "=>" */
}

external var OpTypes : `T$2`

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


external var Types: `T$0`

external fun isASTBaseNode(value: Any): Boolean

external interface ASTBaseNode {
    var type: String
    var location: FileRange
}

external interface ASTBaseParent<STMT : ASTBaseNode> : ASTBaseNode {
    var body: Array<STMT>
}

external interface Literal<T : String?> : ASTBaseNode {
    var value: T
    var quoted: dynamic /* Boolean | "html" */
        get() = definedExternally
        set(value) = definedExternally
}

external interface Literal__0 : Literal<String>

external interface OcDot : ASTBaseParent<dynamic /* OcNet | Comment */>

external interface OcNet : ASTBaseParent<dynamic /* Attribute | Attributes | Edge | Node | Subgraph | Comment */> {
    var id: Literal__0?
        get() = definedExternally
        set(value) = definedExternally
}

external interface KeyValue {
    var key: Literal__0
    var value: Literal__0
}

external interface Attribute : ASTBaseNode, KeyValue

@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
external interface Comment : ASTBaseNode {
    var kind: Kind
    var value: String

    interface `T$1` {
        var Block: String /* "block" */
        var Slash: String /* "slash" */
        var Macro: String /* "macro" */
    }

    companion object {
        var Kind: Readonly<`T$1`>
    }
}

@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
external interface Attributes : ASTBaseParent<dynamic /* Attribute | Comment */> {
    var kind: Kind1

    interface `T$2` {
        var Ocnet: String /* "ocnet" */
        var Edge: String /* "edge" */
        var Node: String /* "node" */
    }

    companion object {
        var Kind: Readonly<`T$2`>
    }
}

external interface NodeRef : ASTBaseNode {
    var id: Literal__0
    var port: Literal__0?
        get() = definedExternally
        set(value) = definedExternally
    var compass: Literal<String? /* "n" | "ne" | "e" | "se" | "s" | "sw" | "w" | "nw" | "c" | "_" */>?
        get() = definedExternally
        set(value) = definedExternally
}

external interface NodeRefGroup : ASTBaseParent<NodeRef>

external interface OpParams {
    var number : Number
}
external interface EdgeOperator : ASTBaseNode {
    override var type: String /* "->" | "=>" */
    var params : OpParams?
}

external interface EdgeRHSElement {
    var id: dynamic /* NodeRef | EdgeSubgraph */
        get() = definedExternally
        set(value) = definedExternally
    var edgeop: EdgeOperator

}

external interface Edge : ASTBaseParent<Attribute> {
    var from: dynamic /* NodeRef | NodeRefGroup */
        get() = definedExternally
        set(value) = definedExternally
    var targets: Array<EdgeRHSElement> /* JsTuple<to, EdgeRHSElement, Any, Array<EdgeRHSElement>> */
}

external interface Node : ASTBaseParent<Attribute> {
    var id: Literal__0
}


external interface Subgraph : ASTBaseParent<dynamic /* Attribute | Attributes | Edge | Node | Subgraph | Comment */> {
    var id: Literal__0?
        get() = definedExternally
        set(value) = definedExternally

    var specialType: String?
        get() = definedExternally
        set(value) = definedExternally

}

external interface ParseOption<T : Any> {
    var rule: T?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ParseOption__0 : ParseOption<Any>

external fun parse(ocdot: String): OcDot

external fun parse(ocdot: String, options: ParseOption<Any>): dynamic /* OcDot */

external fun parse(
    ocdot: String,
    options: ParseOption__0,
): dynamic /* OcDot | OcNet | Attribute | Attributes | Edge | Node | Subgraph | Comment | Array<dynamic /* Attribute | Attributes | Edge | Node | Subgraph | Comment */> */

external interface StringifyOption {
    var indentSize: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Compiler(__0: StringifyOption = definedExternally) {
    open var indentSize: Number
    open fun indent(line: String): String
    open fun pad(pad: String): (l: String) -> String
    open fun printAttribute(ast: Attribute): String
    open fun printAttributes(ast: Attributes): String
    open fun printComment(ast: Comment): String
    open fun printOcDot(ast: OcDot): String
    open fun printEdge(ast: Edge): String
    open fun printEdgeRHSElement(edgeRHSElement: EdgeRHSElement): String
    open fun printNode(ast: Node): String
    open fun printNodeRef(ast: NodeRef): String
    open fun printNodeRefGroup(ast: NodeRefGroup): String
    open fun printOcNet(ast: OcNet): String
    open fun printSubgraph(ast: Subgraph): String
    open fun printLiteral(ast: Literal__0): String

    open var isAstNode: Any
    open fun stringify(ast: Attribute): String
    open fun stringify(ast: Attributes): String
    open fun stringify(ast: Comment): String
    open fun stringify(ast: OcDot): String
    open fun stringify(ast: Edge): String
    open fun stringify(ast: OcNet): String
    open fun stringify(ast: Literal__0): String
    open fun stringify(ast: Node): String
    open fun stringify(ast: NodeRef): String
    open fun stringify(ast: NodeRefGroup): String
    open fun stringify(ast: Subgraph): String
    open fun stringify(ast: EdgeRHSElement): String
}

external fun stringify(ast: Attribute, options: StringifyOption = definedExternally): String

external fun stringify(ast: Attribute): String

external fun stringify(ast: Attributes, options: StringifyOption = definedExternally): String

external fun stringify(ast: Attributes): String

external fun stringify(ast: Comment, options: StringifyOption = definedExternally): String

external fun stringify(ast: Comment): String

external fun stringify(ast: OcDot, options: StringifyOption = definedExternally): String

external fun stringify(ast: OcDot): String

external fun stringify(ast: Edge, options: StringifyOption = definedExternally): String

external fun stringify(ast: Edge): String

external fun stringify(ast: OcNet, options: StringifyOption = definedExternally): String

external fun stringify(ast: OcNet): String

external fun stringify(ast: Literal__0, options: StringifyOption = definedExternally): String

external fun stringify(ast: Literal__0): String

external fun stringify(ast: Node, options: StringifyOption = definedExternally): String

external fun stringify(ast: Node): String

external fun stringify(ast: NodeRef, options: StringifyOption = definedExternally): String

external fun stringify(ast: NodeRef): String

external fun stringify(ast: NodeRefGroup, options: StringifyOption = definedExternally): String

external fun stringify(ast: NodeRefGroup): String

external fun stringify(ast: Subgraph, options: StringifyOption = definedExternally): String

external fun stringify(ast: Subgraph): String
