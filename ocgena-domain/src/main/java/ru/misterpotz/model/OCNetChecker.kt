package model

import error.ConsistencyCheckError
import error.ErrorLevel
import ru.misterpotz.model.validators.ConsistencyCheckPetriAtomVisitorDFS

class OCNetChecker(
    /**
     * places from which all subgraphs of the net are reachable, and the structure is setup
     */
    private val ocNetElements: OCNetElements,
    private val placeTyping: PlaceTyping,
    private val inputOutputPlaces: InputOutputPlaces,
) {
    private var lastConsistencyResults: List<ConsistencyCheckError>? = null
//    private var inputPlaces: List<Place>? = null
//    private var outputPlaces: List<Place>? = null
    val inputPlaces = inputOutputPlaces.getInputPlaces(ocNetElements.places)
    val outputPlaces = inputOutputPlaces.getOutputPlaces(ocNetElements.places)

    val isConsistent: Boolean
        get() {
            return Companion.checkConsistency(lastConsistencyResults)
        }

    fun createWellFormedOCNet(): StaticCoreOcNet {
        require(isConsistent)

        return StaticCoreOcNet(
            inputPlaces = Places(checkNotNull(inputPlaces)),
            outputPlaces = Places(checkNotNull(outputPlaces)),
            objectTypes = ObjectTypes(checkNotNull(placeTyping.allObjectTypes().toList())),
            places = ocNetElements.places,
            transitions = ocNetElements.transitions,
            arcs = ocNetElements.arcs,
            allPetriNodes = ocNetElements.allPetriNodes,
            placeTyping = placeTyping,
            allArcs = ocNetElements.allArcs
        )
    }

    fun checkConsistency(): List<ConsistencyCheckError> {
        val inconsistencies = mutableListOf<ConsistencyCheckError>()

        val createdCheckVisitors = mutableListOf<ConsistencyCheckPetriAtomVisitorDFS>()
        var currentSubgraphIndex = 0

        var maxSubgraphIndex = -1
        // case 1 - parse and check for isolated subgraphs
        for (petriNode in ocNetElements.allPetriNodes) {
            if (petriNode.subgraphIndex in 0..maxSubgraphIndex) {
                // the subgraph of this place was already visited
            } else {
                val visitor = ConsistencyCheckPetriAtomVisitorDFS(
                    assignedSubgraphIndex = currentSubgraphIndex,
                    placeTyping,
                    inputOutputPlaces
                )
                createdCheckVisitors.add(visitor)
                petriNode.acceptVisitor(visitor)
                maxSubgraphIndex = maxSubgraphIndex.coerceAtLeast(visitor.subgraphIndex)
                currentSubgraphIndex = maxSubgraphIndex + 1
            }
        }
        for (visitor in createdCheckVisitors) {
            inconsistencies.addAll(visitor.inconsistenciesSet)
        }

        if (maxSubgraphIndex > 0) {
            inconsistencies.add(ConsistencyCheckError.IsolatedSubgraphsDetected())
        }

        // case 2 - check input places presence
        if (inputPlaces.isEmpty()) {
            inconsistencies.add(ConsistencyCheckError.NoInputPlacesDetected)
        }

        // case 3 - check output places presence

        if (outputPlaces.isEmpty()) {
            inconsistencies.add(ConsistencyCheckError.NoOutputPlacesDetected)
        }

        lastConsistencyResults = inconsistencies
        return inconsistencies
    }

    companion object {
        fun checkConsistency(conistencyResults: List<ConsistencyCheckError>?): Boolean {
            return conistencyResults?.let {
                it.none { it.errorLevel == ErrorLevel.CRITICAL }
            } ?: false
        }
    }
}
