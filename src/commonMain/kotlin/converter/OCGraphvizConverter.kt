package converter

import dsl.OCNetFacadeBuilder
import model.Arc
import model.ConsistencyCheckError
import model.ErrorLevel
import model.NormalArc
import model.VariableArc
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


@OptIn(ExperimentalJsExport::class)
@JsExport
interface FilePosition {
    val offset: Int
    val line: Int
    val column: Int
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface FileRange {
    val start: FilePosition
    val end: FilePosition
    val source: String?
}

@OptIn(ExperimentalJsExport::class)
@JsExport
data class ASTTypeLocation(
    val type: String,
    val location: FileRange,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
data class SemanticError(
    val message: String,
    val relatedAst: ASTTypeLocation,
    val level: ErrorLevel,
)


@OptIn(ExperimentalJsExport::class)
@JsExport
sealed class OCDotParseResult {
    data class SyntaxParseError(val message: String, val location: FileRange?) : OCDotParseResult()

    data class SemanticParseException(
        val message: String,
        val originalException: Throwable?,
    ) : OCDotParseResult()

    data class SemanticCriticalErrorsFound(val message: String, val collectedSemanticErrors: List<SemanticError>) :
        OCDotParseResult()

    data class DomainCheckCriticalErrorsFound(val message: String, val collectedSemanticErrors: List<ConsistencyCheckError>) : OCDotParseResult()
    data class Success(val buildOCNet: OCNetFacadeBuilder.BuiltOCNet) : OCDotParseResult()
}

expect class OCDotParser {
    fun parse(ocDot: String): OCDotParseResult
}

class OCGraphvizGenerator(
    private val originalOCDOtDeclaration: OCDotDeclaration,
    private val ocNetElements: OCNetElements,
    // to understand which nodes should be highlighted
    private val ocnetErrorService: OCNetErrorService,
) {
    private fun Map<String, String>.toAttributeList(): String {
        return map { "${it.key}=${it.value}" }.joinToString(" ")
    }

    private fun Arc.toEdgeStatement(): String {
        val tail = tailNode?.label ?: return ""
        val arrow = arrowNode?.label ?: return ""
        return "$tail -> $arrow"
    }

    fun compileDigraph(): String {
        return """
            |digraph ocnet {
            |${compileDigraphStatements().trimEnd('\n').prependIndent()} 
            |}
        """.trimMargin()
    }

    fun compileDigraphStatements(): String {
        val stringBuilder = StringBuilder()

        // must follow the structure of original declaration (because can contain subgraphs)
        originalOCDOtDeclaration
        // TODO: for transitions and places use different shapes and style
        for (place in ocNetElements.places) {
            val nodeAttributeList = originalOCDOtDeclaration.getNodeAttributeList(place.id)
            stringBuilder.appendLine(
                """
                ${place.label} [ ${nodeAttributeList.toAttributeList()} ];
            """.trimIndent()
            )
        }
        for (transition in ocNetElements.transitions) {
            val nodeAttributeList = originalOCDOtDeclaration.getNodeAttributeList(transition.id)
            stringBuilder.appendLine(
                """
                ${transition.label} [ ${nodeAttributeList.toAttributeList()} ];
            """.trimIndent()
            )
        }
        for (edge in ocNetElements.arcs) {
            val edgeAttributeList = originalOCDOtDeclaration.getEdgeAttributeList(edge.id).toMutableMap()
            val color = edgeAttributeList["color"]?.trim('"')?.split(":")?.first()?.split(";")
            val finalColor = color ?: "black"
            when (edge) {
                is VariableArc -> {
                    edgeAttributeList["color"] = "\"$finalColor:white:$finalColor\""
                }

                is NormalArc -> {
                    edgeAttributeList["color"] = "\"$finalColor\""
                }
            }
            stringBuilder.appendLine(
                """
                ${edge.toEdgeStatement()} [ ${edgeAttributeList.toAttributeList()} ]
            """.trimIndent()
            )
        }
        return stringBuilder.toString()
    }
}
