package model

class OCNetChecker(
    /**
     * at least one place from all possible subgraphs of a net (not
     * necessarily input places)
     */
    private val places : List<Place>
) {
    private var lastConsistencyResults : List<ConsistencyCheckError>? = null
    private var inputPlaces : List<Place>? = null
    private var outputPlaces : List<Place>? = null

    val isConsistent : Boolean
        get() = lastConsistencyResults?.isEmpty() ?: false

//    fun createConsistentOCNet() : OCNet {
//        require(isConsistent)
//        return OCNet(
//            inputPlaces = checkNotNull(inputPlaces),
//            outputPlaces = checkNotNull(outputPlaces),
//            objectTypes = // TODO: pass the object types
//        )
//    }

    fun checkConsistency() : List<ConsistencyCheckError> {
        val inconsistencies = mutableListOf<ConsistencyCheckError>()

        val visitedAtomsSet = mutableSetOf<PetriAtom>()
        val createdCheckVisitors = mutableListOf<ConsistencyCheckPetriAtomVisitor>()

        var currentSubgraphIndex = createdCheckVisitors.size

        // case 1 - parse and check for isolated subgraphs
        for (place in places) {
            if (place in visitedAtomsSet) {
                // the subgraph of this place was already visited
            } else {
                val visitor = ConsistencyCheckPetriAtomVisitor(
                    assignedSubgraphIndex = currentSubgraphIndex
                )
                createdCheckVisitors.add(visitor)
                place.acceptVisitor(visitor)
                visitedAtomsSet.addAll(visitor.visitedSet)
                currentSubgraphIndex = createdCheckVisitors.size
            }
        }

        for (i in 2..createdCheckVisitors.size) {
            val checkVisitor = createdCheckVisitors[i - 1]
            inconsistencies.add(
                ConsistencyCheckError.IsolatedSubgraphsDetected(
                    checkVisitor.visitedSet.toList()
                )
            )
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
        lastConsistencyResults = inconsistencies
        if (isConsistent) {
            inputPlaces = allInputPlaces
            outputPlaces = allOutputPlaces
        } else {
            inputPlaces = null
            outputPlaces = null
        }
        return inconsistencies
    }
}
