package converter.visitors

import converter.ASTVisitorPath

interface PathAcceptorVisitorBFS {
    fun withPath(astVisitorPath: ASTVisitorPath): OCDotASTVisitorBFS
}
