package converter

import ast.Edge
import ast.EdgeRHSElement
import ast.OpTypes
import ast.Types
import dsl.OCNetFacadeBuilder
import model.*


data class ConversionParams(
    val placeTyping: PlaceTyping,
    val inputOutputPlaces: InputOutputPlaces,
    val dslElementsContainer: StructureContainer,
    val useType: OcNetType
)

class OCDotToDomainConverter(
    private val conversionParams: ConversionParams,
) {
    private val initialMarking = ObjectMarking()
    val conversionContainer = ConversionEntitiesCreator(
        conversionParams.placeTyping
    )
    val arcConversionCreator = ArcConversionCreator(conversionContainer)

    fun convert(): OCNetFacadeBuilder.BuiltOCNet {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val placeTyping = conversionParams.placeTyping
        val inputOutputPlaces = conversionParams.inputOutputPlaces
        val dslElementsContainer = conversionParams.dslElementsContainer


        for (astPlace in dslElementsContainer.savedPlaces) {
            val placeLabel = astPlace.key
            console.log("place label $placeLabel")
            conversionContainer.recordPlace(astPlace.key)
        }

        for (astTransition in dslElementsContainer.savedTransitions) {
            conversionContainer.recordTransition(astTransition.key)
        }

        for (edge in dslElementsContainer.savedEdgeBlocks) {
            processEdge(edge)
        }

        val resultOfBuildAttempt = ocNetFacadeBuilder.tryBuildModelFromOcNetElements(
            placeTyping = placeTyping,
            inputOutputPlaces = inputOutputPlaces,
            ocNetElements = conversionContainer.buildOcNetElements()
        )
        return resultOfBuildAttempt
    }

    private fun processEdge(edge: Edge) {
        val connector = Connector(
            edge = edge,
            conversionEntitiesCreator = conversionContainer,
            arcConversionCreator = arcConversionCreator
        )
        connector.tryConnectAll()
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
