package ru.misterpotz.ocgena.validation

import error.ConsistencyCheckError
import ru.misterpotz.ocgena.ocnet.primitives.PlaceType
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArcTypeA
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry

class ConsistencyCheckPetriAtomVisitorDFS(
    val assignedSubgraphIndex: Int,
    private val placeTypeRegistry: PlaceTypeRegistry,
    private val petriAtomRegistry: PetriAtomRegistry,
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
        val subgraphIndex = petriAtomRegistry.getSubgraphIndex(petriAtom.id)
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
        recordIfArcConsistencyErrors(arc)
        return checkIfExistsSubgraphIndex(arc)
    }

    override fun doForTransitionBeforeDFS(transition: Transition): Boolean {
        recordIfTransitionConsistencyErrors(transition)
        return checkIfExistsSubgraphIndex(transition)
    }

    override fun doForPlaceBeforeDFS(place: Place): Boolean {
        recordIfPlaceConsistencyErrors(place)
        return checkIfExistsSubgraphIndex(place)
    }

    private fun recordIfArcConsistencyErrors(arc: Arc) {
        val tailNodeId = arc.tailNodeId
        val arrowNodeId = arc.arrowNodeId

        if (tailNodeId == null || arrowNodeId == null) {
            inconsistenciesSet.add(
                ConsistencyCheckError.MissingNode(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath()
                )
            )
        }
        if (tailNodeId == arrowNodeId) {
            inconsistenciesSet.add(
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
                inconsistenciesSet.add(
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
            if (transition.inputPlaces.isEmpty() ||
                transition.outputPlaces.isEmpty()
            ) {
                inconsistenciesSet.add(
                    ConsistencyCheckError.MissingArc(
                        transition = transition.id,
                        debugPath = copyAndAppendTraversalPath()
                    )
                )
            }
            // case 2 - only one arc goes from a place to transition
            val visitedPlaces = mutableSetOf<PetriAtomId>()
            for (inputPlaceId in transition.inputPlaces) {
                if (inputPlaceId in visitedPlaces) {
                    // one place has 2 or more arcs to transition
                    inconsistenciesSet.add(
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
            for (outputPlaceId in transition.outputPlaces) {
                if (outputPlaceId in visitedPlaces) {
                    inconsistenciesSet.add(
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
            for (inputPlaceId in transition.inputPlaces) {
                for (outputPlaceId in transition.outputPlaces) {
                    val inputPlaceType = placeTypeRegistry[inputPlaceId]
                    val outputPlaceType = placeTypeRegistry[outputPlaceId]

                    if (inputPlaceType == outputPlaceType) {
                        // arcs must be of same type
                        val inputArc = inputPlaceId.arcTo(transition.id)
                        val outputArc = transition.id.arcTo(outputPlaceId)
                        val arcsOfSameType = inputArc.isSameArcType(outputArc)

                        if (!arcsOfSameType) {
                            inconsistenciesSet.add(
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
            if (transition.inputPlaces.size == 1) {
                val firstInputPlace = transition.inputPlaces.first()
                if (firstInputPlace.arcTo(transition.id) is VariableArcTypeA) {
                    inconsistenciesSet.add(
                        ConsistencyCheckError.VariableArcIsTheOnlyConnected(
                            transition = transition.id,
                            debugPath = copyAndAppendTraversalPath()
                        )
                    )
                }
            }
            if (transition.outputPlaces.size == 1) {
                val firstOutputPlace = transition.outputPlaces.first()

                if (transition.id.arcTo(firstOutputPlace) is VariableArcTypeA) {
                    inconsistenciesSet.add(
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
            inconsistenciesSet.add(
                ConsistencyCheckError.IsolatedPlace(
                    place = place.id,
                    debugPath = copyAndAppendTraversalPath()
                )
            )
        }

        // case 2 - input place has input arcs
        if (placeTypeRegistry[place.id] == PlaceType.INPUT && place.fromTransitions.isNotEmpty()) {
            inconsistenciesSet.add(
                ConsistencyCheckError.InputPlaceHasInputArcs(place.id, copyAndAppendTraversalPath())
            )
        }
        // case 3 - output place has output arcs
        if (placeTypeRegistry[place.id] == PlaceType.OUTPUT && place.toTransitions.isNotEmpty()) {
            inconsistenciesSet.add(
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
