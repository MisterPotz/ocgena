package converter

import ast.Edge
import ast.Node
import ast.OcDot
import ast.OcNet
import ast.Subgraph

class ElementsDeclarationsASTVisitorBFS(
    val dslElementsContainer: DSLElementsContainer,
    private val semanticErrorReporter: SemanticErrorReporterContainer = SemanticErrorReporterContainer(),
) : PathAcceptingASTVisitorBFS(), SemanticErrorReporter by semanticErrorReporter {
    private val normalSubgraphHelper = NormalSubgraphHelper(dslElementsContainer, semanticErrorReporter)
    private val specialSubgraphHelper = SpecialSubgraphHelper(dslElementsContainer, semanticErrorReporter)

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

    override fun collectReport(): List<SemanticErrorAST> {
        return semanticErrorReporter.collectReport()
    }
}
