package converter

interface PathAcceptorVisitorBFS {
    fun withPath(astVisitorPath: ASTVisitorPath): OCDotASTVisitorBFS
}
