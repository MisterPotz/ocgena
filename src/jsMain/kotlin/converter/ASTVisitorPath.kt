package converter

import ast.ASTBaseNode
import ast.Types

class ASTVisitorPath(val path: MutableList<ASTBaseNode>) {
    fun push(ast: ASTBaseNode) {
        path.add(ast)
    }

    fun pop(ast: ASTBaseNode) {
        if (path.isEmpty()) return
        path.removeLast()
    }

    fun isAtBlockOf(type: dynamic /* Types */): Boolean {
        return path.last().type == type
    }

    fun isAtOcNet(): Boolean {
        return isAtBlockOf(Types.Ocnet)
    }

    fun isAtSubgraph(): Boolean {
        return isAtBlockOf(Types.Subgraph)
    }
}
