package converter.subgraph

import ast.ASTBaseNode
import ast.Node
import ast.SubgraphSpecialTypes
import converter.DSLElementsContainer
import converter.SemanticDomainErrorReporterContainer

class PlaceSaver(
    private val dslElementsContainer: DSLElementsContainer,
    errorReporterContainer: SemanticDomainErrorReporterContainer,
) :
    SubgraphElementSaver(SubgraphSpecialTypes.Places, errorReporterContainer) {
    override fun saveNode(ast: ASTBaseNode) {
        if (checkNodeIsOk(ast)) {
            dslElementsContainer.rememberPlace(ast as Node)
        }
    }
}
