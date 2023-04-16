package converter

import ast.Edge
import ast.Node
import ast.OcDot
import ast.OcNet
import ast.Subgraph

interface OCDotASTVisitorBFS {
    fun visitOCDot(ast: OcDot)
    fun visitOCNet(ast: OcNet)
    fun visitNode(ast: Node)
    fun visitEdge(ast: Edge)
    fun visitSubgraph(ast: Subgraph)
}
