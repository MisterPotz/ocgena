package converter.subgraph

import ast.Subgraph
import ast.SubgraphSpecialTypes
import converter.StructureContainer
import converter.ErrorReporterContainer
import error.ErrorLevel
import parse.SemanticError

class SpecialSubgraphHelper(
    private val dslElementsContainer: StructureContainer,
    private val errorReporterContainer: ErrorReporterContainer,
) {
    private val elementSavers: Map<String /* SubgraphSpecialTypes */, SubgraphElementSaver> = buildMap {
        put(SubgraphSpecialTypes.Places, PlaceSaver(dslElementsContainer, errorReporterContainer))
        put(SubgraphSpecialTypes.Transitions, TransitionSaver(dslElementsContainer, errorReporterContainer))
    }

    private val subgraphHitCount = SpecialSubgraphsHitCounter()


    fun trySaveSubgraphEntities(ast: Subgraph) {
        val specialType = ast.specialType
        if (specialType != null) {
            val elementSaver = elementSavers[specialType] ?: return
            val fullGraphName = SpecialSubgraphsHitCounter.getFullNameFor(specialType, ast.id?.value)
            console.log("checking hits for $fullGraphName")
            if (!subgraphHitCount.canHit(fullGraphName)) {
                errorReporterContainer.pushError(
                    SemanticError(
                        "has already encountered block of this type: ${ast.specialType}",
                        relatedAst = ast,
                        errorLevel = ErrorLevel.WARNING
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
