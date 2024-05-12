package ru.misterpotz.ocgena.validation

import ru.misterpotz.ocgena.error.ConsistencyCheckError
import ru.misterpotz.ocgena.ocnet.primitives.PlaceType
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry

class ConsistencyCheckPetriAtomVisitorDFS(
    val assignedSubgraphIndex: Int,
    private val placeTypeRegistry: PlaceTypeRegistry,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val petriAtomRegistry: PetriAtomRegistry,
    private val loggingEnabled : Boolean
) : AbsPetriAtomVisitorDFS(
    petriAtomRegistry
) {
    val inconsistenciesSet: MutableList<ConsistencyCheckError> = mutableListOf()

    private var discoveredSubgraphIndex: Int? = null

    val subgraphIndex: Int
        get() = discoveredSubgraphIndex ?: assignedSubgraphIndex

    val visitedSet: MutableSet<PetriAtomId>
        get() = recursionProtector.visitedSet

    private fun checkIfExistsSubgraphIndex(petriAtom: PetriAtom): Boolean {
        val subgraphIndex = with(petriAtomRegistry) {
            petriAtom.id.subgraphIndex
        }
        if (subgraphIndex != assignedSubgraphIndex && subgraphIndex != null) {
            discoveredSubgraphIndex = subgraphIndex
            return true
        }
        return false
    }

    override fun doForAtomAfterDFS(atom: PetriAtom) {
        setSubgraphIndexTo(atom)
    }

    override fun doForArcBeforeDFS(arc: Arc): Boolean {
        logItem(arc)
        recordIfArcConsistencyErrors(arc)
        return checkIfExistsSubgraphIndex(arc)
    }

    override fun doForTransitionBeforeDFS(transition: Transition): Boolean {
        logItem(transition)
        recordIfTransitionConsistencyErrors(transition)
        return checkIfExistsSubgraphIndex(transition)
    }

    private fun logItem(petriAtom: PetriAtom) {
        if (loggingEnabled) {
            println("hit $petriAtom")
        }
    }

    private fun recInconsistency(consistencyCheckError: ConsistencyCheckError) {
        if (loggingEnabled){
            println("found error: $consistencyCheckError")
        }
        inconsistenciesSet.add(consistencyCheckError)
    }

    override fun doForPlaceBeforeDFS(place: Place): Boolean {
        logItem(place)
        recordIfPlaceConsistencyErrors(place)
        return checkIfExistsSubgraphIndex(place)
    }

    private fun recordIfArcConsistencyErrors(arc: Arc) {
        val tailNodeId = arc.tailNodeId
        val arrowNodeId = arc.arrowNodeId

        if (tailNodeId == null || arrowNodeId == null) {
            recInconsistency(
                ConsistencyCheckError.MissingNode(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath()
                )
            )
        }
        if (tailNodeId == arrowNodeId) {
            recInconsistency(
                ConsistencyCheckError.ArcInputEqualsOutput(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath()
                )
            )
        }
        with(petriAtomRegistry) {
            if (tailNodeId != null &&
                arrowNodeId != null &&
                (arrowNodeId.asNode())
                    .isSameType(tailNodeId.asNode())
            ) {
                recInconsistency(
                    ConsistencyCheckError.IsNotBipartite(
                        arc = arc,
                        debugPath = copyAndAppendTraversalPath()
                    )
                )
            }
        }


    }

    private fun setSubgraphIndexTo(petriAtom: PetriAtom) {
        with(petriAtomRegistry) {
            petriAtom.id.subgraphIndex = subgraphIndex
        }
    }

    private fun recordIfTransitionConsistencyErrors(transition: Transition) {
        with(petriAtomRegistry) {
            // case 1
            if (transition.fromPlaces.isEmpty() ||
                transition.toPlaces.isEmpty()
            ) {
                recInconsistency(
                    ConsistencyCheckError.MissingArc(
                        transition = transition.id,
                        debugPath = copyAndAppendTraversalPath()
                    )
                )
            }
            // case 2 - only one arc goes from a place to transition
            val visitedPlaces = mutableSetOf<PetriAtomId>()
            for (inputPlaceId in transition.fromPlaces) {
                if (inputPlaceId in visitedPlaces) {
                    // one place has 2 or more arcs to transition
                    recInconsistency(
                        ConsistencyCheckError.MultipleArcsFromSinglePlaceToSingleTransition(
                            place = inputPlaceId,
                            transition = transition.id,
                            debugPath = copyAndAppendTraversalPath()
                        )
                    )
                } else {
                    visitedPlaces.add(inputPlaceId)
                }
            }
            visitedPlaces.clear()
            for (outputPlaceId in transition.toPlaces) {
                if (outputPlaceId in visitedPlaces) {
                    recInconsistency(
                        ConsistencyCheckError.MultipleArcsFromSinglePlaceToSingleTransition(
                            place = outputPlaceId,
                            transition = transition.id,
                            debugPath = copyAndAppendTraversalPath()
                        )
                    )
                } else {
                    visitedPlaces.add(outputPlaceId)
                }
            }

            // case 3 - variability arcs inconsistency
            for (inputPlaceId in transition.fromPlaces) {
                for (outputPlaceId in transition.toPlaces) {
                    val inputPlaceType = placeToObjectTypeRegistry[inputPlaceId]
                    val outputPlaceType = placeToObjectTypeRegistry[outputPlaceId]

                    if (inputPlaceType == outputPlaceType) {
                        // arcs must be of same type
                        val inputArc = inputPlaceId.arcTo(transition.id)
                        val outputArc = transition.id.arcTo(outputPlaceId)
                        val arcsOfSameType = inputArc.isSameArcType(outputArc)

                        if (!arcsOfSameType) {
                            recInconsistency(
                                ConsistencyCheckError.InconsistentVariabilityArcs(
                                    transition = transition.id,
                                    inputArc = inputArc,
                                    outputArc = outputArc
                                )
                            )
                        }
                    }
                }
            }

            // case 4 - only variability arc serves as input or output arc
            if (transition.fromPlaces.size == 1) {
                val firstInputPlace = transition.fromPlaces.first()
                if (firstInputPlace.arcTo(transition.id) is VariableArc) {
                    recInconsistency(
                        ConsistencyCheckError.VariableArcIsTheOnlyConnected(
                            transition = transition.id,
                            debugPath = copyAndAppendTraversalPath()
                        )
                    )
                }
            }
            if (transition.toPlaces.size == 1) {
                val firstOutputPlace = transition.toPlaces.first()

                if (transition.id.arcTo(firstOutputPlace) is VariableArc) {
                    recInconsistency(
                        ConsistencyCheckError.VariableArcIsTheOnlyConnected(
                            transition = transition.id,
                            debugPath = copyAndAppendTraversalPath()
                        )
                    )
                }
            }
        }
    }

    private fun recordIfPlaceConsistencyErrors(place: Place) {
        // case 1 - isolated place
        if (place.toTransitions.isEmpty() && place.fromTransitions.isEmpty()) {
            recInconsistency(
                ConsistencyCheckError.IsolatedPlace(
                    place = place.id,
                    debugPath = copyAndAppendTraversalPath()
                )
            )
        }

        // case 2 - input place has input arcs
//        if (placeTypeRegistry[place.id] == PlaceType.INPUT && place.fromTransitions.isNotEmpty()) {
//            recInconsistency(
//                ConsistencyCheckError.InputPlaceHasInputArcs(place.id, copyAndAppendTraversalPath())
//            )
//        }
        // case 3 - output place has output arcs
        if (placeTypeRegistry[place.id] == PlaceType.OUTPUT && place.toTransitions.isNotEmpty()) {
            recInconsistency(
                ConsistencyCheckError.OutputPlaceHasOutputArcs(place.id, copyAndAppendTraversalPath())
            )
        }
    }

    private fun copyAndAppendTraversalPath(): List<PetriAtomId> {
        return recursionProtector.recursiveStack.toList()
    }

    private fun protectWithRecursionStack(petriAtom: PetriAtom, block: () -> Unit) {
        recursionProtector.protectWithRecursionStack(petriAtom.id, block)
    }
}
