package converter

import ast.ASTBaseNode
import ast.Attribute
import ast.Node
import ast.Subgraph
import ast.SubgraphSpecialTypes
import ast.Types
import model.ErrorLevel

class SpecialSubgraphHelper(
    private val dslElementsContainer: DSLElementsContainer,
    private val errorReporterContainer: SemanticErrorReporterContainer,
) {
    private val elementSavers: Map<String /* SubgraphSpecialTypes */, SubgraphElementSaver> = buildMap {
        put(SubgraphSpecialTypes.Places, PlaceSaver(dslElementsContainer, errorReporterContainer))
        put(SubgraphSpecialTypes.Transitions, TransitionSaver(dslElementsContainer, errorReporterContainer))
        put(SubgraphSpecialTypes.ObjectTypes, ObjectTypeSaver(dslElementsContainer, errorReporterContainer))
        put(SubgraphSpecialTypes.InitialMarking, InitialMarkingSaver(dslElementsContainer, errorReporterContainer))
        put(SubgraphSpecialTypes.PlacesForType, PlacesForTypeSaver(dslElementsContainer, errorReporterContainer))
        put(SubgraphSpecialTypes.Inputs, InputsForTypeSaver(dslElementsContainer, errorReporterContainer))
        put(SubgraphSpecialTypes.Outputs, OutputsForTypeSaver(dslElementsContainer, errorReporterContainer))
    }

    private val subgraphHitCount = SpecialSubgraphsHitCounter()


    class InputsForTypeSaver(
        private val dslElementsContainer: DSLElementsContainer,
        errorReporterContainer: SemanticErrorReporterContainer,
    ) : SubgraphElementSaver(
        SubgraphSpecialTypes.InitialMarking, errorReporterContainer
    ) {
        override fun saveNode(ast: ASTBaseNode) {
            if (checkNodeIsOk(ast)) {
                val casted = ast as Node

                dslElementsContainer.rememberPlaceIsInput(casted.id.value)
            }
        }
    }

    class OutputsForTypeSaver(
        private val dslElementsContainer: DSLElementsContainer,
        errorReporterContainer: SemanticErrorReporterContainer,
    ) : SubgraphElementSaver(
        SubgraphSpecialTypes.InitialMarking, errorReporterContainer
    ) {
        override fun saveNode(ast: ASTBaseNode) {
            if (checkNodeIsOk(ast)) {
                val casted = ast as Node

                dslElementsContainer.rememberPlaceIsOutput(casted.id.value)
            }
        }
    }

    class PlacesForTypeSaver(
        private val dslElementsContainer: DSLElementsContainer,
        errorReporterContainer: SemanticErrorReporterContainer,
    ) : SubgraphElementSaver(
        SubgraphSpecialTypes.InitialMarking, errorReporterContainer
    ) {
        override fun saveNode(ast: ASTBaseNode) {
            if (checkNodeIsOk(ast)) {
                val casted = ast as Node

                val objectType = subgraphName ?: return

                dslElementsContainer.rememberObjectTypeForPlace(
                    casted.id.value,
                    objectType
                )
            }
        }
    }

    class InitialMarkingSaver(
        private val dslElementsContainer: DSLElementsContainer,
        errorReporterContainer: SemanticErrorReporterContainer,
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
                    SemanticErrorAST(
                        "expected node or comment, but encountered different type: ${ast.type}",
                        relatedAst = ast,
                        level = ErrorLevel.WARNING
                    )
                )
                return false
            }
            return checkNodeCanBeSaved(ast)
        }

    }

    class PlaceSaver(
        private val dslElementsContainer: DSLElementsContainer,
        errorReporterContainer: SemanticErrorReporterContainer,
    ) :
        SubgraphElementSaver(SubgraphSpecialTypes.Places, errorReporterContainer) {
        override fun saveNode(ast: ASTBaseNode) {
            if (checkNodeIsOk(ast)) {
                dslElementsContainer.rememberPlace(ast as Node)
            }
        }
    }

    class TransitionSaver(
        private val dslElementsContainer: DSLElementsContainer,
        errorReporterContainer: SemanticErrorReporterContainer,
    ) : SubgraphElementSaver(
        SubgraphSpecialTypes.Transitions, errorReporterContainer
    ) {
        override fun saveNode(ast: ASTBaseNode) {
            if (checkNodeIsOk(ast)) {
                dslElementsContainer.rememberTransition(ast as Node)
            }
        }
    }

    class ObjectTypeSaver(
        private val dslElementsContainer: DSLElementsContainer,
        errorReporterContainer: SemanticErrorReporterContainer,
    ) : SubgraphElementSaver(
        SubgraphSpecialTypes.ObjectTypes, errorReporterContainer
    ) {
        override fun saveNode(ast: ASTBaseNode) {
            if (checkNodeIsOk(ast)) {
                dslElementsContainer.rememberObjectType(ast as Node)
            }
        }
    }

    class SpecialSubgraphsHitCounter {
        val specialSubgraphTypeToHits = mutableMapOf<String, Int>()


        fun hit(specialSubgraphType: String) {
            specialSubgraphTypeToHits[specialSubgraphType] = (specialSubgraphTypeToHits[specialSubgraphType] ?: 0) + 1
        }

        fun hitsFor(specialSubgraphType: String) : Int {
            return specialSubgraphTypeToHits[specialSubgraphType] ?: 0
        }

        fun canHit(specialSubgraphType: String): Boolean {
            val current = hitsFor(specialSubgraphType)
            return when (specialSubgraphType) {
                SubgraphSpecialTypes.ObjectTypes,
                SubgraphSpecialTypes.Places,
                SubgraphSpecialTypes.Transitions,
                SubgraphSpecialTypes.InitialMarking,
                SubgraphSpecialTypes.Inputs,
                SubgraphSpecialTypes.Outputs -> current == 0
                else -> {
                    if (specialSubgraphType.startsWith(SubgraphSpecialTypes.PlacesForType)) {
                        current == 0
                    } else {
                        false
                    }
                }
            }
        }

        companion object {
            fun getFullNameFor(specialSubgraphType: String, subgraphId : String?) : String {
                return specialSubgraphType+(subgraphId ?: "")
            }
        }
    }

    abstract class SubgraphElementSaver(
        val subgraphSpecialType: dynamic, /* SubgraphSpecialTypes */
        val semanticErrorReporterContainer: SemanticErrorReporterContainer,
    ) : SemanticErrorReporter by semanticErrorReporterContainer {
        abstract fun saveNode(ast: ASTBaseNode)
        private var hasSaved: Boolean = false
        protected var subgraphName: String? = null
        protected fun pushError(error: SemanticErrorAST) {
            semanticErrorReporterContainer.pushError(error)
        }

        fun setSubgraphName(subgraph: Subgraph) {
            subgraphName = subgraph.id?.value
        }

        fun reset() {
            hasSaved = false
        }

        fun saveNodes(ast: Subgraph) {
            for (i in ast.body) {
                saveNode(i)
            }
        }

        protected open fun checkNodeIsAcceptable(ast: ASTBaseNode): Boolean {
            return ast.type == Types.Node || ast.type == Types.Comment
        }

        protected open fun checkNodeCanBeSaved(ast: ASTBaseNode): Boolean {
            return ast.type == Types.Node
        }

        protected open fun checkNodeIsOk(ast: ASTBaseNode): Boolean {
            if (!checkNodeIsAcceptable(ast)) {
                pushError(
                    SemanticErrorAST(
                        "expected node or comment, but encountered different type: ${ast.type}",
                        relatedAst = ast,
                        level = ErrorLevel.WARNING
                    )
                )
                return false
            }
            return checkNodeCanBeSaved(ast)
        }
    }

    fun trySaveSubgraphEntities(ast: Subgraph) {
        val specialType = ast.specialType
        if (specialType != null) {
            val elementSaver = elementSavers[specialType] ?: return
            val fullGraphName = SpecialSubgraphsHitCounter.getFullNameFor(specialType, ast.id?.value)
            console.log("checking hits for $fullGraphName")
            if (!subgraphHitCount.canHit(fullGraphName)) {
                errorReporterContainer.pushError(
                    SemanticErrorAST(
                        "has already encountered block of this type: ${ast.specialType}",
                        relatedAst = ast,
                        level = ErrorLevel.WARNING
                    )
                )
            } else {
                subgraphHitCount.hit(fullGraphName)
                elementSaver.setSubgraphName(ast)
                elementSaver.saveNodes(ast)
            }
        }
    }

    fun reset() {
        elementSavers.values.forEach {
            it.reset()
        }
    }
}
