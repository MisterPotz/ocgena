package converter.visitors

import ast.ASTBaseNode
import ast.Edge
import ast.Node
import ast.OcDot
import ast.OcNet
import ast.Subgraph
import ast.SubgraphSpecialTypes
import ast.Types
import converter.ErrorReporterContainer
import converter.DefaultErrorReporterContainer
import error.Error
import error.ErrorLevel
import parse.SemanticError

class StructureCheckASTVisitorBFS(
    private val errorReporterContainer: ErrorReporterContainer,
) : PathAcceptingASTVisitorBFS(), ErrorReporterContainer by errorReporterContainer {

    override fun pushError(error: Error) {
        errorReporterContainer.pushError(error)
    }

    private fun countElements(array: Array<ASTBaseNode>, type: dynamic): Int {
        return array.count { it.type == type }
    }

    override fun visitOCDot(ast: OcDot) {
        val ocNets = countElements(ast.body, Types.Ocnet)

        if (ocNets > 1) {
            pushError(
                SemanticError(
                    message = "Only 1 ocnet block is allowed",
                    relatedAst = ast,
                    errorLevel = ErrorLevel.CRITICAL
                )
            )
        }
        if (ocNets == 0) {
            pushError(
                SemanticError(
                    message = "At least 1 ocnet block must be defined",
                    relatedAst = ast,
                    errorLevel = ErrorLevel.WARNING
                )
            )
        }
    }

    override fun visitOCNet(ast: OcNet) {
        val placesBlockCount = ast.body.count {
            it.type == Types.Subgraph
                    && (it as Subgraph).specialType == SubgraphSpecialTypes.Places
        }

        val transitionBlockCount = ast.body.count {
            it.type == Types.Subgraph
                    && (it as Subgraph).specialType == SubgraphSpecialTypes.Transitions
        }

        if (placesBlockCount == 0 || transitionBlockCount == 0) {
            pushError(
                SemanticError(
                    message = "places or transitions blocks are missing",
                    relatedAst = ast,
                    errorLevel = ErrorLevel.WARNING
                )
            )
        }
    }

    override fun visitNode(ast: Node) {

    }

    override fun visitEdge(ast: Edge) {

    }

    override fun visitSubgraph(ast: Subgraph) {

    }
}
