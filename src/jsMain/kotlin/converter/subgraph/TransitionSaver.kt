package converter.subgraph

import ast.ASTBaseNode
import ast.Node
import ast.SubgraphSpecialTypes
import converter.DSLElementsContainer
import converter.SemanticDomainErrorReporterContainer

class TransitionSaver(
    private val dslElementsContainer: DSLElementsContainer,
    errorReporterContainer: SemanticDomainErrorReporterContainer,
) : SubgraphElementSaver(
    SubgraphSpecialTypes.Transitions, errorReporterContainer
) {
    override fun saveNode(ast: ASTBaseNode) {
        if (checkNodeIsOk(ast)) {
            dslElementsContainer.rememberTransition(ast as Node)
        }
    }
}
