package converter

import ast.ASTBaseNode

abstract class PathAcceptingASTVisitorBFS() : OCDotASTVisitorBFS, PathAcceptorVisitorBFS {
    protected var path: ASTVisitorPath? = null
    protected val currentPath: ASTVisitorPath
        get() = path!!

    val last: ASTBaseNode
        get() = currentPath.path.last()

    override fun withPath(astVisitorPath: ASTVisitorPath): OCDotASTVisitorBFS {
        path = astVisitorPath
        return this
    }
}
