package converter.visitors

import ast.ASTBaseNode
import ast.Attribute
import ast.SubgraphSpecialTypes
import ast.Types
import converter.DSLElementsContainer
import converter.SemanticDomainErrorReporterContainer
import converter.subgraph.SubgraphElementSaver
import error.ErrorLevel
import parse.SemanticError

class InitialMarkingSaver(
    private val dslElementsContainer: DSLElementsContainer,
    errorReporterContainer: SemanticDomainErrorReporterContainer,
) : SubgraphElementSaver(
    SubgraphSpecialTypes.InitialMarking, errorReporterContainer
) {
    override fun saveNode(ast: ASTBaseNode) {
        if (checkNodeIsOk(ast)) {
            val casted = ast as Attribute
            val tokensAttempt = casted.value.value.toIntOrNull() ?: return

            dslElementsContainer.rememberInitialMarkingForPlace(casted.key.value, tokensAttempt)
        }
    }

    override fun checkNodeIsAcceptable(ast: ASTBaseNode): Boolean {
        return ast.type == Types.Attribute || ast.type == Types.Comment
    }

    override fun checkNodeCanBeSaved(ast: ASTBaseNode): Boolean {
        return ast.type == Types.Attribute
    }

    override fun checkNodeIsOk(ast: ASTBaseNode): Boolean {
        if (!checkNodeIsAcceptable(ast)) {
            pushError(
                SemanticError(
                    "expected node or comment, but encountered different type: ${ast.type}",
                    relatedAst = ast,
                    errorLevel = ErrorLevel.WARNING
                )
            )
            return false
        }
        return checkNodeCanBeSaved(ast)
    }
}
