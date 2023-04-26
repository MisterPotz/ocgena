package converter.subgraph

import ast.ASTBaseNode
import ast.Node
import ast.SubgraphSpecialTypes
import converter.StructureContainer
import converter.ErrorReporterContainer

class TransitionSaver(
    private val dslElementsContainer: StructureContainer,
    errorReporterContainer: ErrorReporterContainer,
) : SubgraphElementSaver(
    SubgraphSpecialTypes.Transitions, errorReporterContainer
) {
    override fun saveNode(ast: ASTBaseNode) {
        if (checkNodeIsOk(ast)) {
            dslElementsContainer.rememberTransition(ast as Node)
        }
    }
}
