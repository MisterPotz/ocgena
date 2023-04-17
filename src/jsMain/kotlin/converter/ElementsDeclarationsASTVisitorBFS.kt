package converter

import ast.Edge
import ast.Node
import ast.OcDot
import ast.OcNet
import ast.Subgraph
import error.Error

class ElementsDeclarationsASTVisitorBFS(
    val dslElementsContainer: DSLElementsContainer,
    private val errorReporter: SemanticDomainErrorReporterContainer = SemanticDomainErrorReporterContainer(),
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
