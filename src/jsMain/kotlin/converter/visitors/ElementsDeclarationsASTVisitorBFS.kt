package converter.visitors

import ast.*
import converter.ErrorReporterContainer
import converter.StructureContainer
import converter.subgraph.NormalSubgraphHelper
import converter.subgraph.SpecialSubgraphHelper
import error.Error

class ElementsDeclarationsASTVisitorBFS(
    private val dslElementsContainer: StructureContainer,
    private val errorReporter: ErrorReporterContainer,
) : PathAcceptingASTVisitorBFS(), ErrorReporterContainer by errorReporter {
    private val normalSubgraphHelper = NormalSubgraphHelper(dslElementsContainer, errorReporter)
    private val specialSubgraphHelper = SpecialSubgraphHelper(dslElementsContainer, errorReporter)

    override fun visitOCDot(ast: OcDot) {}

    override fun visitOCNet(ast: OcNet) {}

    override fun visitNode(ast: Node) {

    }

    override fun visitEdge(ast: Edge) {
        dslElementsContainer.rememberEdgeBlock(ast)
    }

    override fun visitSubgraph(ast: Subgraph) {
        specialSubgraphHelper.trySaveSubgraphEntities(ast)
        normalSubgraphHelper.trySaveSubgraph(ast)
    }

    override fun collectReport(): List<Error> {
        return errorReporter.collectReport()
    }
}
