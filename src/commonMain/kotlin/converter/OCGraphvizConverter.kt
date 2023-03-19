package converter

import dsl.ObjectTypeDSL
import model.Arc
import model.ConsistencyCheckError
import model.NormalArc
import model.ObjectType
import model.PetriNode
import model.Place
import model.Transition
import model.VariableArc

interface OCNetElements {
    val places: List<Place>

    val transitions: List<Transition>

    val arcs: List<Arc>

    val allPetriNodes: List<PetriNode>

    val objectTypes : List<ObjectType>
}


interface OCNetErrorService {
    // observed consistency errors for given petri id
    fun errorsForPetriAtomId(petriAtomId: String): List<ConsistencyCheckError>
}

object EmptyOCNetErrorService : OCNetErrorService {
    override fun errorsForPetriAtomId(petriAtomId: String): List<ConsistencyCheckError> {
        return emptyList()
    }
}

//expect class OCGraphvizConverter {
//    fun renderFile()
//}

interface OCDotDeclaration {
    // assumption: it is ordered list of statements
    // plain - doesn't contain any data that is relevant to simulation
    fun getNodeAttributeList(nodeLabel: String): Map<String, String>

    // plain - doesn't contain any data that is relevant to simulation
    fun getEdgeAttributeList(edgeLabel: String): Map<String, String>
}

class DefaultOCNetDeclaration(
    // in form key:"t1" value:"color=\"green\" ... other attributes"
    val nodeAttributes: Map<String /* id */, String> = mapOf(),
    val edgeAttributes: Map<String /* id */, String> = mapOf(),
) : OCDotDeclaration {
    override fun getNodeAttributeList(nodeLabel: String): Map<String, String> {
        return nodeAttributes[nodeLabel]?.split(" ")?.map {
            val keyAndValue = it.split("=")
            keyAndValue[0] to keyAndValue[1]
        }?.let {
            buildMap {
                for (i in it) {
                    put(i.first, i.second)
                }
            }
        } ?: mapOf()
    }

    override fun getEdgeAttributeList(edgeLabel: String): Map<String, String> {
        return nodeAttributes[edgeLabel]?.split(" ")?.map {
            val keyAndValue = it.split("=")
            keyAndValue[0] to keyAndValue[1]
        }?.let {
            buildMap {
                for (i in it) {
                    put(i.first, i.second)
                }
            }
        } ?: mapOf()
    }

}

// probably it would be better to generate domain models at the same time
// with editing the original declaration
// but for now lets forget about the optimization lol
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
            stringBuilder.appendLine("""
                ${place.label} [ ${nodeAttributeList.toAttributeList()} ];
            """.trimIndent()
            )
        }
        for (transition in ocNetElements.transitions) {
            val nodeAttributeList = originalOCDOtDeclaration.getNodeAttributeList(transition.id)
            stringBuilder.appendLine("""
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
            stringBuilder.appendLine("""
                ${edge.toEdgeStatement()} [ ${edgeAttributeList.toAttributeList()} ]
            """.trimIndent()
            )
        }
        return stringBuilder.toString()
    }
}
