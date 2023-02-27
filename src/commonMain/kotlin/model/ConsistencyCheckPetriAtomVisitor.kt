package model

class ConsistencyCheckPetriAtomVisitor(
    val assignedSubgraphIndex: Int,
) : PetriAtomVisitor {
    private val recursionProtector = RecursionProtector()
    val obtainedOutputPlaces: MutableList<Place> = mutableListOf()
    val obtainedInputPlaces: MutableList<Place> = mutableListOf()

    private val inconsistenciesSet: MutableList<ConsistencyCheckError> = mutableListOf()

    val visitedSet: MutableSet<PetriAtom>
        get() = recursionProtector.visitedSet

    override fun visitArc(arc: Arc) {
        protectWithRecursionStack(arc) {
            setSubgraphIndexTo(arc)
            recordIfArcConsistencyErrors(arc)
            arc.arrowNode?.let { arrowNode ->
                when (arrowNode) {
                    is Transition -> visitTransition(arrowNode)
                    is Place -> visitPlace(arrowNode)
                    else -> throw IllegalStateException("unsupported arrowNode $arrowNode of type ${arrowNode::class}")
                }
            }
        }
    }

    override fun visitTransition(transition: Transition) {
        protectWithRecursionStack(transition) {
            setSubgraphIndexTo(transition)
            recordIfTransitionConsistencyErrors(transition)
            for (outputArc in transition.outputArcs) {
                visitArc(outputArc)
            }
        }
    }

    override fun visitPlace(place: Place) {
        protectWithRecursionStack(place) {
            setSubgraphIndexTo(place)
            recordIfPlaceConsistencyErrors(place)
            for (outputArc in place.outputArcs) {
                visitArc(outputArc)
            }
        }
    }

    private fun recordIfArcConsistencyErrors(arc: Arc) {
        if (arc.tailNode == null || arc.arrowNode == null) {
            inconsistenciesSet.add(
                ConsistencyCheckError.MissingNode(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath(arc)
                )
            )
        }
        if (arc.tailNode == arc.arrowNode) {
            inconsistenciesSet.add(
                ConsistencyCheckError.ArcInputEqualsOutput(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath(arc)
                )
            )
        }
    }

    private fun setSubgraphIndexTo(petriAtom: PetriAtom) {
        petriAtom.subgraphIndex = assignedSubgraphIndex
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
        return recursionProtector.recursiveStack.toMutableList().apply {
            add(atom)
        }.toList()
    }

    private fun protectWithRecursionStack(petriAtom: PetriAtom, block: () -> Unit) {
        recursionProtector.protectWithRecursionStack(petriAtom, block)
    }
}
