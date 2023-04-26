package converter.subgraph

import ast.ASTBaseNode
import ast.Node
import ast.SubgraphSpecialTypes
import converter.DSLElementsContainer
import converter.SemanticDomainErrorReporterContainer

class InputsForTypeSaver(
    private val dslElementsContainer: DSLElementsContainer,
    errorReporterContainer: SemanticDomainErrorReporterContainer,
) : SubgraphElementSaver(
    SubgraphSpecialTypes.InitialMarking, errorReporterContainer
) {
    override fun saveNode(ast: ASTBaseNode) {
        if (checkNodeIsOk(ast)) {
            val casted = ast as Node

            dslElementsContainer.rememberPlaceIsInput(casted.id.value)
        }
    }
}
