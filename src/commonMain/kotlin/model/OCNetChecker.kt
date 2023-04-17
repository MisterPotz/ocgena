package model

import error.ConsistencyCheckError
import error.ErrorLevel
import model.utils.ConsistencyCheckPetriAtomVisitorDFS

class OCNetChecker(
    /**
     * places from which all subgraphs of the net are reachable, and the structure is setup
     */
    private val allPetriNodes : List<PetriNode>,
) {
    private var lastConsistencyResults : List<ConsistencyCheckError>? = null
    private var inputPlaces : List<Place>? = null
    private var outputPlaces : List<Place>? = null
    private var objectTypes : List<ObjectType>? = null

    val isConsistent : Boolean
        get() {
            return Companion.checkConsistency(lastConsistencyResults)
        }

    fun createWellFormedOCNet() : WellFormedOCNet {
        require(isConsistent)
        return WellFormedOCNet(
            inputPlaces = checkNotNull(inputPlaces),
            outputPlaces = checkNotNull(outputPlaces),
            objectTypes = checkNotNull(objectTypes)// TODO: pass the object types
        )
    }

    fun checkConsistency() : List<ConsistencyCheckError> {
        val inconsistencies = mutableListOf<ConsistencyCheckError>()

        val createdCheckVisitors = mutableListOf<ConsistencyCheckPetriAtomVisitorDFS>()
        var currentSubgraphIndex = 0

        var maxSubgraphIndex = -1
        // case 1 - parse and check for isolated subgraphs
        for (petriNode in allPetriNodes) {
            if (petriNode.subgraphIndex in 0..maxSubgraphIndex) {
                // the subgraph of this place was already visited
            } else {
                val visitor = ConsistencyCheckPetriAtomVisitorDFS(
                    assignedSubgraphIndex = currentSubgraphIndex
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
        val allInputPlaces = mutableListOf<Place>()
        for (visitor in createdCheckVisitors) {
            allInputPlaces.addAll(visitor.obtainedInputPlaces)
        }
        if (allInputPlaces.isEmpty()) {
            inconsistencies.add(ConsistencyCheckError.NoInputPlacesDetected)
        }

        // case 3 - check output places presence
        val allOutputPlaces = mutableListOf<Place>()
        for (visitor in createdCheckVisitors) {
            allOutputPlaces.addAll(visitor.obtainedOutputPlaces)
        }
        if (allOutputPlaces.isEmpty()) {
            inconsistencies.add(ConsistencyCheckError.NoOutputPlacesDetected)
        }
        val allObjectTypes = mutableSetOf<ObjectType>()
        for (visitor in createdCheckVisitors) {
            allObjectTypes.addAll(visitor.obtainedObjectTypes)
        }

        lastConsistencyResults = inconsistencies
        if (isConsistent) {
            inputPlaces = allInputPlaces
            outputPlaces = allOutputPlaces
            objectTypes = allObjectTypes.toList()
        } else {
            inputPlaces = null
            outputPlaces = null
            objectTypes = null
        }
        return inconsistencies
    }

    companion object {
        fun checkConsistency(conistencyResults: List<ConsistencyCheckError>?) : Boolean {
            return conistencyResults?.let {
                it.none { it.errorLevel == ErrorLevel.CRITICAL }
            } ?: false
        }
    }
}
