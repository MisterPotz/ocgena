package model

class OCNetChecker(
    /**
     * places from which all subgraphs of the net are reachable, and the structure is setup
     */
    private val places : List<Place>
) {
    private var lastConsistencyResults : List<ConsistencyCheckError>? = null
    private var inputPlaces : List<Place>? = null
    private var outputPlaces : List<Place>? = null
    private var objectTypes : List<ObjectType>? = null

    val isConsistent : Boolean
        get() = lastConsistencyResults?.isEmpty() ?: false

    fun createConsistentOCNet() : OCNet {
        require(isConsistent)
        return OCNet(
            inputPlaces = checkNotNull(inputPlaces),
            outputPlaces = checkNotNull(outputPlaces),
            objectTypes = checkNotNull(objectTypes)// TODO: pass the object types
        )
    }

    fun checkConsistency() : List<ConsistencyCheckError> {
        val inconsistencies = mutableListOf<ConsistencyCheckError>()

        val createdCheckVisitors = mutableListOf<ConsistencyCheckPetriAtomVisitor>()
        var currentSubgraphIndex = 0

        var maxSubgraphIndex = -1
        // case 1 - parse and check for isolated subgraphs
        for (place in places) {
            if (place.subgraphIndex in 0..maxSubgraphIndex) {
                // the subgraph of this place was already visited
            } else {
                val visitor = ConsistencyCheckPetriAtomVisitor(
                    assignedSubgraphIndex = currentSubgraphIndex
                )
                createdCheckVisitors.add(visitor)
                place.acceptVisitor(visitor)
                maxSubgraphIndex = maxSubgraphIndex.coerceAtLeast(visitor.subgraphIndex)
                currentSubgraphIndex = maxSubgraphIndex + 1
            }
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
}
