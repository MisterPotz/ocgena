package converter

import ast.ASTBaseNode
import ast.Node
import ast.Subgraph
import ast.SubgraphSpecialTypes
import ast.Types
import model.ErrorLevel

class SpecialSubgraphHelper(
    private val dslElementsContainer: DSLElementsContainer,
    private val errorReporterContainer: SemanticErrorReporterContainer,
) {
    private val elementSavers : Map<String /* SubgraphSpecialTypes */, SubgraphElementSaver> = buildMap {
        put(SubgraphSpecialTypes.Places, PlaceSaver(dslElementsContainer, errorReporterContainer))
        put(SubgraphSpecialTypes.Transitions, TransitionSaver(dslElementsContainer, errorReporterContainer))
        put(SubgraphSpecialTypes.ObjectTypes, ObjectTypeSaver(dslElementsContainer, errorReporterContainer))
    }

    class PlaceSaver(private val dslElementsContainer: DSLElementsContainer, errorReporterContainer: SemanticErrorReporterContainer) :
        SubgraphElementSaver(SubgraphSpecialTypes.Places, errorReporterContainer) {
        override fun saveNode(ast: ASTBaseNode) {
            if (checkNodeIsOk(ast)) {
                dslElementsContainer.rememberPlace(ast as Node)
            }
        }
    }

    class TransitionSaver(private val dslElementsContainer: DSLElementsContainer, errorReporterContainer: SemanticErrorReporterContainer) : SubgraphElementSaver(
        SubgraphSpecialTypes.Transitions, errorReporterContainer) {
        override fun saveNode(ast: ASTBaseNode) {
            if (checkNodeIsOk(ast)) {
                dslElementsContainer.rememberTransition(ast as Node)
            }
        }
    }

    class ObjectTypeSaver(private val dslElementsContainer: DSLElementsContainer, errorReporterContainer: SemanticErrorReporterContainer) : SubgraphElementSaver(
        SubgraphSpecialTypes.ObjectTypes, errorReporterContainer) {
        override fun saveNode(ast: ASTBaseNode) {
            if (checkNodeIsOk(ast)) {
                dslElementsContainer.rememberObjectType(ast as Node)
            }
        }
    }

    abstract class SubgraphElementSaver(
        val subgraphSpecialType: dynamic, /* SubgraphSpecialTypes */
        val semanticErrorReporterContainer: SemanticErrorReporterContainer,
    ) : SemanticErrorReporter by semanticErrorReporterContainer {
        abstract fun saveNode(ast: ASTBaseNode)

        protected fun pushError(error: SemanticError) {
            semanticErrorReporterContainer.pushError(error)
        }

        fun saveNodes(ast: Subgraph) {
            for (i in ast.body) {
                saveNode(i)
            }
        }

        private fun checkNodeIsAcceptable(ast: ASTBaseNode): Boolean {
            return ast.type == Types.Node || ast.type == Types.Comment
        }

        protected fun checkNodeIsOk(ast: ASTBaseNode) : Boolean {
            if (!checkNodeIsAcceptable(ast)) {
                pushError(
                    SemanticError(
                        "expected node or comment, but encountered different type: ${ast.type}",
                        relatedAst = ast,
                        level = ErrorLevel.WARNING
                    )
                )
                return false
            }
            return true
        }
    }

    fun trySaveSubgraphEntities(ast : Subgraph) {
        val specialType = ast.specialType
        if (specialType != null) {
            elementSavers[specialType]?.saveNodes(ast)
        }
    }
}
