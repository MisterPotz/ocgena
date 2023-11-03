package ru.misterpotz.ocgena.validation

import error.ConsistencyCheckError
import error.ErrorLevel
import model.OCNetElements
import model.StaticCoreOcNet
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry

class OCNetChecker(
    /**
     * places from which all subgraphs of the net are reachable, and the structure is setup
     */
    private val ocNetElements: OCNetElements,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val placeTypeRegistry: PlaceTypeRegistry,
    private val petriAtomRegistry: PetriAtomRegistry
) {
    private var lastConsistencyResults: List<ConsistencyCheckError>? = null
    val inputPlaces = placeTypeRegistry.getInputPlaces(ocNetElements.places)
    val outputPlaces = placeTypeRegistry.getOutputPlaces(ocNetElements.places)

    val isConsistent: Boolean
        get() {
            return checkConsistency(lastConsistencyResults)
        }

    fun createWellFormedOCNet(): StaticCoreOcNet {
        require(isConsistent)

        return StaticCoreOcNet(
            inputPlaces = require(inputPlaces.isEmpty().not()).let { inputPlaces },
            outputPlaces = require(outputPlaces.isEmpty().not()).let { outputPlaces },
            objectTypes = placeToObjectTypeRegistry.allObjectTypes().toMutableList(),
            places = ocNetElements.places,
            transitionsRegistry = ocNetElements.transitionsRegistry,
            arcsRegistry = ocNetElements.arcsRegistry,
            placeToObjectTypeRegistry = placeToObjectTypeRegistry,
        )
    }

    fun checkConsistency(): List<ConsistencyCheckError> {
        val inconsistencies = mutableListOf<ConsistencyCheckError>()

        val createdCheckVisitors = mutableListOf<ConsistencyCheckPetriAtomVisitorDFS>()
        var currentSubgraphIndex = 0

        var maxSubgraphIndex = -1
        // case 1 - parse and check for isolated subgraphs
        for (petriNodeId in petriAtomRegistry.iterator) {
            val petriNode = petriAtomRegistry[petriNodeId]
            val subgraphIndex = petriAtomRegistry.getSubgraphIndex(petriNodeId)

            if (subgraphIndex in 0..maxSubgraphIndex) {
                // the subgraph of this place was already visited
            } else {
                val visitor = ConsistencyCheckPetriAtomVisitorDFS(
                    assignedSubgraphIndex = currentSubgraphIndex,
                    placeTypeRegistry = placeTypeRegistry,
                    petriAtomRegistry = petriAtomRegistry
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
