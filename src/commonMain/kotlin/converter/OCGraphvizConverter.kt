package converter

import model.Arc
import model.NormalArc
import model.VariableArc

// TODO: move these logics to typescript as this requires directly the original dot, which is not available in kotlin js part of the project

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
