package converter

import ast.Edge
import ast.EdgeRHSElement
import ast.OpTypes
import ast.Types
import dsl.OCNetFacadeBuilder
import dsl.OCScope
import model.InputOutputPlaces
import model.ObjectMarking
import model.PlaceType
import model.PlaceTyping

class OCDotToDomainConverter(
    private val placeTyping: PlaceTyping,
    private val inputOutputPlaces: InputOutputPlaces,
    val dslElementsContainer: StructureContainer,
) {
    private val initialMarking = ObjectMarking()

    fun convert(): OCNetFacadeBuilder.BuiltOCNet {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val resultOfBuildAttempt = ocNetFacadeBuilder.tryBuildModel(
            placeTyping = placeTyping,
            inputOutputPlaces = inputOutputPlaces
        ) {
            for (astPlace in dslElementsContainer.savedPlaces) {
                val placeLabel = astPlace.key
                console.log("place label $placeLabel")
                place(astPlace.key)
            }

            for (astTransition in dslElementsContainer.savedTransitions) {
                transition(astTransition.key) {
                    // initialize transition data
                }
            }

            for (edge in dslElementsContainer.savedEdgeBlocks) {
                processEdge(edge)
            }
        }
        return resultOfBuildAttempt
    }

    fun OCScope.processEdge(edge: Edge) {
        val connector = Connector(edge)
        with(connector) {
            tryConnectAll()
        }
    }

    companion object {
        fun isEdgeRhs(from: dynamic): Boolean {
            return when ((from as? EdgeRHSElement)?.edgeop?.type) {
                OpTypes.Normal, OpTypes.Variable -> true
                else -> false
            }
        }

        fun isNodeRef(from: dynamic): Boolean {
            return from.type == Types.NodeRef
        }
    }
}
