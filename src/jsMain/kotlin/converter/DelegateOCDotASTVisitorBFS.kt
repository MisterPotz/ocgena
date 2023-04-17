package converter

import ast.ASTBaseNode
import ast.Edge
import ast.Node
import ast.OcDot
import ast.OcNet
import ast.Subgraph
import ast.Types

class DelegateOCDotASTVisitorBFS(
    private val visitors: List<PathAcceptorVisitorBFS>,
) : OCDotASTVisitorBFS {
    private val currentPath = ASTVisitorPath(mutableListOf())

    override fun visitOCDot(ast: OcDot) {
        currentPath.push(ast)
        for (astNode in ast.body) {
            val castAstNode = astNode as ASTBaseNode
            when (castAstNode.type) {
                Types.Ocnet -> {
                    doDelegateVisit(castAstNode as OcNet)
                    visitOCNet(castAstNode)
                }

                Types.Comment -> {
                    // skip
                }
            }
        }
        currentPath.pop(ast)
    }

    private fun <T : ASTBaseNode> doDelegateVisit(astNode: T) {
        for (visitor in visitors) {
            val visitorForPath = visitor.withPath(currentPath)

            with(visitorForPath) {
                when (astNode.type) {
                    Types.Ocnet -> visitOCNet(astNode as OcNet)
                    Types.Attribute -> Unit
                    Types.Attributes -> Unit
                    Types.Edge -> visitEdge(astNode as Edge)
                    Types.Subgraph -> visitSubgraph(astNode as Subgraph)
                    Types.Node -> visitNode(astNode as Node)
                    else -> Unit
                }
            }
        }
    }

    override fun visitOCNet(ast: OcNet) {
        currentPath.push(ast)

        for (stmt in ast.body) {
            val castNode = stmt as ASTBaseNode
            doDelegateVisit(castNode)

        }

        currentPath.pop(ast)
    }

    override fun visitNode(ast: Node) = Unit

    override fun visitEdge(ast: Edge) = Unit

    override fun visitSubgraph(ast: Subgraph) = Unit
}
