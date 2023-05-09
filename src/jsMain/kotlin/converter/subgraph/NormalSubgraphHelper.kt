package converter.subgraph

import ast.ASTBaseNode
import ast.Subgraph
import ast.SubgraphSpecialTypes
import ast.Types
import converter.StructureContainer
import converter.ErrorReporterContainer

class NormalSubgraphHelper(
    val dslElementsContainer: StructureContainer,
    val errorReporterContainer: ErrorReporterContainer,
) : ErrorReporterContainer by errorReporterContainer {

    fun checkElementCanBeSaved(ast: ASTBaseNode): Boolean {
        return ast.type == Types.Node || ast.type == Types.Edge
    }

    val Subgraph.isTransitionsBlock: Boolean
        get() = specialType == SubgraphSpecialTypes.Transitions

    val Subgraph.isPlacesBlock: Boolean
        get() = specialType == SubgraphSpecialTypes.Places

    val Subgraph.isSpecial: Boolean
        get() = specialType != null

    fun checkNodeIsAcceptable(ast: ASTBaseNode): Boolean {
        return when (ast.type) {
            Types.Attribute, Types.Attributes, Types.Edge, Types.Node, Types.Subgraph, Types.Comment -> {
                true
            }

            else -> {
                false
            }
        }
    }

    fun trySaveSubgraph(ast: Subgraph) {
        if (ast.isSpecial) return
        dslElementsContainer.rememberSubgraph(ast)
    }
}
