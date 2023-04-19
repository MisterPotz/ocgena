package converter.subgraph

import ast.ASTBaseNode
import ast.Node
import ast.SubgraphSpecialTypes
import converter.DSLElementsContainer
import converter.SemanticDomainErrorReporterContainer

class ObjectTypeSaver(
    private val dslElementsContainer: DSLElementsContainer,
    errorReporterContainer: SemanticDomainErrorReporterContainer,
) : SubgraphElementSaver(
    SubgraphSpecialTypes.ObjectTypes, errorReporterContainer
) {
    override fun saveNode(ast: ASTBaseNode) {
        if (checkNodeIsOk(ast)) {
            dslElementsContainer.rememberObjectType(ast as Node)
        }
    }
}
