package converter.visitors

import ast.Edge
import ast.Node
import ast.OcDot
import ast.OcNet
import ast.Subgraph
import converter.StructureContainer
import converter.ErrorReporterContainer
import converter.subgraph.NormalSubgraphHelper
import converter.DefaultErrorReporterContainer
import converter.subgraph.SpecialSubgraphHelper
import error.Error

class ElementsDeclarationsASTVisitorBFS(
    val dslElementsContainer: StructureContainer,
    private val errorReporter: ErrorReporterContainer = DefaultErrorReporterContainer(),
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
