package converter.subgraph

import ast.ASTBaseNode
import ast.Subgraph
import ast.Types
import converter.ErrorReporterContainer
import converter.DefaultErrorReporterContainer
import error.ErrorLevel
import parse.SemanticError

abstract class SubgraphElementSaver(
    val subgraphSpecialType: dynamic, /* SubgraphSpecialTypes */
    val semanticErrorReporterContainer: ErrorReporterContainer,
) : ErrorReporterContainer by semanticErrorReporterContainer {
    abstract fun saveNode(ast: ASTBaseNode)
    private var hasSaved: Boolean = false
    protected var subgraphName: String? = null
    protected fun pushError(error: SemanticError) {
        semanticErrorReporterContainer.pushError(error)
    }

    fun setSubgraphName(subgraph: Subgraph) {
        subgraphName = subgraph.id?.value
    }

    fun reset() {
        hasSaved = false
    }

    fun saveNodes(ast: Subgraph) {
        for (i in ast.body) {
            saveNode(i)
        }
    }

    protected open fun checkNodeIsAcceptable(ast: ASTBaseNode): Boolean {
        return ast.type == Types.Node || ast.type == Types.Comment
    }

    protected open fun checkNodeCanBeSaved(ast: ASTBaseNode): Boolean {
        return ast.type == Types.Node
    }

    protected open fun checkNodeIsOk(ast: ASTBaseNode): Boolean {
        if (!checkNodeIsAcceptable(ast)) {
            pushError(
                SemanticError(
                    "expected node or comment, but encountered different type: ${ast.type}",
                    relatedAst = ast,
                    errorLevel = ErrorLevel.WARNING
                )
            )
            return false
        }
        return checkNodeCanBeSaved(ast)
    }
}
