package model

class ConsistencyCheckPetriAtomVisitor(
    val assignedSubgraphIndex: Int,
) : PetriAtomVisitor {
    private val recursionProtector = RecursionProtector()
    val obtainedOutputPlaces: MutableList<Place> = mutableListOf()
    val obtainedInputPlaces: MutableList<Place> = mutableListOf()
    val obtainedObjectTypes: MutableSet<ObjectType> = mutableSetOf()
    val inconsistenciesSet: MutableList<ConsistencyCheckError> = mutableListOf()

    private var discoveredSubgraphIndex: Int? = null

    val subgraphIndex: Int
        get() = discoveredSubgraphIndex ?: assignedSubgraphIndex

    val visitedSet: MutableSet<PetriAtom>
        get() = recursionProtector.visitedSet

    private fun checkIfExistsSubgraphIndex(petriAtom: PetriAtom) : Boolean {
        if (petriAtom.subgraphIndex != assignedSubgraphIndex
            && petriAtom.subgraphIndex != PetriAtom.UNASSIGNED_SUBGRAPH_INDEX
        ) {
            discoveredSubgraphIndex = petriAtom.subgraphIndex
            return true
        }
        return false
    }

    override fun visitArc(arc: Arc) {
        protectWithRecursionStack(arc) {
            recordIfArcConsistencyErrors(arc)
            val foundParsedSubgraph = checkIfExistsSubgraphIndex(arc)
            if (!foundParsedSubgraph) {
                arc.arrowNode?.let { arrowNode ->
                    when (arrowNode) {
                        is Transition -> visitTransition(arrowNode)
                        is Place -> visitPlace(arrowNode)
                        else -> throw IllegalStateException("unsupported arrowNode $arrowNode of type ${arrowNode::class}")
                    }
                }
            }
            setSubgraphIndexTo(arc)
        }
    }

    override fun visitTransition(transition: Transition) {
        protectWithRecursionStack(transition) {
            recordIfTransitionConsistencyErrors(transition)
            val foundParsedSubgraph = checkIfExistsSubgraphIndex(transition)
            if (!foundParsedSubgraph) {
                for (outputArc in transition.outputArcs) {
                    visitArc(outputArc)
                }
            }
            setSubgraphIndexTo(transition)
        }
    }

    override fun visitPlace(place: Place) {
        protectWithRecursionStack(place) {
            savePlaceData(place)
            recordIfPlaceConsistencyErrors(place)
            val foundParsedSubgraph = checkIfExistsSubgraphIndex(place)
            if (!foundParsedSubgraph) {
                for (outputArc in place.outputArcs) {
                    visitArc(outputArc)
                }
            }
            setSubgraphIndexTo(place)
        }
    }

    private fun savePlaceData(place: Place) {
        obtainedObjectTypes.add(place.type)
        when (place.placeType) {
            PlaceType.INPUT -> obtainedInputPlaces.add(place)
            PlaceType.OUTPUT -> obtainedOutputPlaces.add(place)
            PlaceType.NORMAL -> Unit
        }
    }

    private fun recordIfArcConsistencyErrors(arc: Arc) {
        val tailNode = arc.tailNode
        val arrowNode = arc.arrowNode

        if (tailNode == null || arrowNode == null) {
            inconsistenciesSet.add(
                ConsistencyCheckError.MissingNode(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath(arc)
                )
            )
        }
        if (tailNode == arrowNode) {
            inconsistenciesSet.add(
                ConsistencyCheckError.ArcInputEqualsOutput(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath(arc)
                )
            )
        }

        if (tailNode != null && arrowNode != null && arrowNode.isSameType(tailNode)) {
            inconsistenciesSet.add(
                ConsistencyCheckError.IsNotBipartite(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath(arc)
                )
            )
        }
    }

    private fun setSubgraphIndexTo(petriAtom: PetriAtom) {
        petriAtom.subgraphIndex = subgraphIndex
    }

    private fun recordIfTransitionConsistencyErrors(transition: Transition) {
        // case 1
        if (transition.inputArcs.isEmpty() || transition.outputArcs.isEmpty()) {
            inconsistenciesSet.add(
                ConsistencyCheckError.MissingArc(
                    transition = transition,
                    debugPath = copyAndAppendTraversalPath(transition)
                )
            )
        }
        // case 2 - only one arc goes from a place to transition
        val visitedPlaces = mutableSetOf<Place>()
        for (inputArc in transition.inputArcs) {
            val inputPlace = (inputArc.tailNode ?: continue) as Place
            if (inputPlace in visitedPlaces) {
                // one place has 2 or more arcs to transition
                inconsistenciesSet.add(
                    ConsistencyCheckError.MultipleArcsFromSinglePlaceToSingleTransition(
                        place = inputPlace,
                        transition = transition,
                        debugPath = copyAndAppendTraversalPath(transition)
                    )
                )
            } else {
                visitedPlaces.add(inputPlace)
            }
        }
        visitedPlaces.clear()
        for (outputArc in transition.outputArcs) {
            val outputPlace = (outputArc.arrowNode ?: continue) as Place
            if (outputPlace in visitedPlaces) {
                inconsistenciesSet.add(
                    ConsistencyCheckError.MultipleArcsFromSinglePlaceToSingleTransition(
                        place = outputPlace,
                        transition = transition,
                        debugPath = copyAndAppendTraversalPath(transition)
                    )
                )
            }
        }

        // case 3 - variability arcs inconsistency
        val inputPlaces = transition.inputPlaces
        val outputPlaces = transition.outputPlaces

        for (inputArc in transition.inputArcs) {
            val inputPlace = (inputArc.tailNode ?: continue) as Place
            for (outputArc in transition.outputArcs) {
                val outputPlace = (outputArc.arrowNode ?: continue) as Place

                if (inputPlace.type == outputPlace.type) {
                    // arcs must be of same type
                    val arcsOfSameType = inputArc.isSameArcType(outputArc)

                    if (!arcsOfSameType) {
                        inconsistenciesSet.add(
                            ConsistencyCheckError.InconsistentVariabilityArcs(
                                transition = transition,
                                inputArc = inputArc,
                                outputArc = outputArc
                            )
                        )
                    }
                }
            }
        }

        // case 4 - only variability arc serves as input or output arc
        if (transition.inputArcs.size == 1) {
            val firstArc = transition.inputArcs.first()
            if (firstArc is VariableArc) {
                inconsistenciesSet.add(
                    ConsistencyCheckError.VariableArcIsTheOnlyConnected(
                        transition = transition,
                        debugPath = copyAndAppendTraversalPath(transition)
                    )
                )
            }
        }
        if (transition.outputArcs.size == 1) {
            val firstArc = transition.outputArcs.first()
            if (firstArc is VariableArc) {
                inconsistenciesSet.add(
                    ConsistencyCheckError.VariableArcIsTheOnlyConnected(
                        transition = transition,
                        debugPath = copyAndAppendTraversalPath(transition)
                    )
                )
            }
        }
    }

    private fun recordIfPlaceConsistencyErrors(place: Place) {
        // case 1 - isolated place
        if (place.outputArcs.isEmpty() && place.inputArcs.isEmpty()) {
            inconsistenciesSet.add(
                ConsistencyCheckError.IsolatedPlace(
                    place = place,
                    debugPath = copyAndAppendTraversalPath(place)
                )
            )
        }
        // case 2 - input place has input arcs
        if (place.placeType == PlaceType.INPUT && place.inputArcs.isNotEmpty()) {
            inconsistenciesSet.add(
                ConsistencyCheckError.InputPlaceHasInputArcs(place, copyAndAppendTraversalPath(place))
            )
        }
        // case 3 - output place has output arcs
        if (place.placeType == PlaceType.OUTPUT && place.outputArcs.isNotEmpty()) {
            inconsistenciesSet.add(
                ConsistencyCheckError.OutputPlaceHasOutputArcs(place, copyAndAppendTraversalPath(place))
            )
        }
    }

    private fun copyAndAppendTraversalPath(atom: PetriAtom): List<PetriAtom> {
        return recursionProtector.recursiveStack.toList()
    }

    private fun protectWithRecursionStack(petriAtom: PetriAtom, block: () -> Unit) {
        recursionProtector.protectWithRecursionStack(petriAtom, block)
    }
}
