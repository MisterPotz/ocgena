package converter

import ast.Edge
import ast.EdgeRHSElement
import ast.OpTypes
import ast.Types
import dsl.OCNetFacadeBuilder
import dsl.OCScope
import model.ObjectMarking
import model.PlaceType

class OCDotToDomainConverter() {

    private val initialMarking = ObjectMarking()

    fun convert(dslElementsContainer: DSLElementsContainer): OCNetFacadeBuilder.BuiltOCNet {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val resultOfBuildAttempt = ocNetFacadeBuilder.tryBuildModel {
            for (astObjectType in dslElementsContainer.savedObjectTypes) {
                objectType(astObjectType.key)
            }
            for (astPlace in dslElementsContainer.savedPlaces) {
                val placeLabel = astPlace.key
                console.log("place label $placeLabel")
                val place = place(astPlace.key) {
                    val objectType = dslElementsContainer.recallObjectTypeForPlace(label)
                    if (objectType != null) {
                        this.objectType = objectType(objectType)
                    }
                    val initMarking = dslElementsContainer.recallInitialTokensForPlace(label)
                    if (initMarking != null) {
                        this.initialTokens = initMarking
                    }

                    val isInput = dslElementsContainer.recallIfPlaceIsInput(placeLabel = label)
                    val isOutput = dslElementsContainer.recallIfPlaceIsOutput(placeLabel = label)

                    if (isInput) {
                        this.placeType = PlaceType.INPUT
                    }
                    if (isOutput) {
                        this.placeType = PlaceType.OUTPUT
                    }
                }
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
