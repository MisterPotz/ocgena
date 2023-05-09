package converter.subgraph

import ast.ASTBaseNode
import ast.Node
import ast.SubgraphSpecialTypes
import converter.StructureContainer
import converter.ErrorReporterContainer

class PlaceSaver(
    private val dslElementsContainer: StructureContainer,
    errorReporterContainer: ErrorReporterContainer,
) :
    SubgraphElementSaver(SubgraphSpecialTypes.Places, errorReporterContainer) {
    override fun saveNode(ast: ASTBaseNode) {
        if (checkNodeIsOk(ast)) {
            dslElementsContainer.rememberPlace(ast as Node)
        }
    }
}
