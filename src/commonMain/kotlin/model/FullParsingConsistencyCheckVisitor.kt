package model

class FullParsingConsistencyCheckVisitor(
    val assignedSubgraphIndex : Int,
) : ParsingConsistencyCheckVisitor {
    private class VisitedAndRecursiveStacks(
        val visitedSet: MutableSet<PetriAtom> = mutableSetOf(),
        val recursiveStack: MutableList<PetriAtom> = mutableListOf()
    )
    private val cyclicStacks : VisitedAndRecursiveStacks = VisitedAndRecursiveStacks()
    val obtainedOutputPlaces : MutableList<Place> = mutableListOf()
    val obtainedInputPlaces : MutableList<Place> = mutableListOf()

    private val inconsistenciesSet: MutableList<ConsistencyCheckError> = mutableListOf()

    val visitedSet : MutableSet<PetriAtom>
        get() = cyclicStacks.visitedSet

    override fun checkConsistencyForArc(arc: Arc) {
        setSubgraphIndexTo(arc)
        recordIfArcConsistencyErrors(arc)
        protectWithRecursionStack(arc) {
            arc.arrowNode?.let { arrowNode ->
                when (arrowNode) {
                    is Transition -> checkConsistencyForTransition(arrowNode)
                    is Place -> checkConsistencyForPlace(arrowNode)
                    else -> throw IllegalStateException("unsupported arrowNode $arrowNode of type ${arrowNode::class}")
                }
            }
        }
    }

    override fun checkConsistencyForTransition(transition: Transition) {
        setSubgraphIndexTo(transition)
        recordIfTransitionConsistencyErrors(transition)
        protectWithRecursionStack(transition) {
            for (outputArc in transition.outputArcs) {
                checkConsistencyForArc(outputArc)
            }
        }
    }

    override fun checkConsistencyForPlace(place: Place) {
        setSubgraphIndexTo(place)
        recordIfPlaceConsistencyErrors(place)
        protectWithRecursionStack(place) {
            for (outputArc in place.outputArcs) {
                checkConsistencyForArc(outputArc)
            }
        }
    }

    private fun recordIfArcConsistencyErrors(arc: Arc) {
        if (arc.tailNode == null || arc.arrowNode == null) {
            inconsistenciesSet.add(
                ConsistencyCheckError.MissingNode(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath(cyclicStacks.recursiveStack, arc)
                )
            )
        }
        if (arc.tailNode == arc.arrowNode) {
            inconsistenciesSet.add(
                ConsistencyCheckError.ArcInputEqualsOutput(
                    arc = arc,
                    debugPath = copyAndAppendTraversalPath(cyclicStacks.recursiveStack, arc)
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
                    debugPath = copyAndAppendTraversalPath(cyclicStacks.recursiveStack, transition)
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
                        debugPath = copyAndAppendTraversalPath(cyclicStacks.recursiveStack, transition)
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
                        debugPath = copyAndAppendTraversalPath(cyclicStacks.recursiveStack, transition)
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
                        debugPath = copyAndAppendTraversalPath(cyclicStacks.recursiveStack, transition)
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
                        debugPath = copyAndAppendTraversalPath(cyclicStacks.recursiveStack, transition)
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
                    debugPath = copyAndAppendTraversalPath(cyclicStacks.recursiveStack, place)
                )
            )
        }
        // case 2 - input place has input arcs
        if (place.placeType == PlaceType.INPUT && place.inputArcs.isNotEmpty()) {
            inconsistenciesSet.add(
                ConsistencyCheckError.InputPlaceHasInputArcs(place, copyAndAppendTraversalPath(cyclicStacks.recursiveStack, place))
            )
        }
        // case 3 - output place has output arcs
        if (place.placeType == PlaceType.OUTPUT && place.outputArcs.isNotEmpty()) {
            inconsistenciesSet.add(
                ConsistencyCheckError.OutputPlaceHasOutputArcs(place, copyAndAppendTraversalPath(cyclicStacks.recursiveStack, place))
            )
        }
    }

    private fun copyAndAppendTraversalPath(recursiveStack : List<PetriAtom>, atom: PetriAtom): List<PetriAtom> {
        return recursiveStack.toMutableList().apply {
            add(atom)
        }.toList()
    }

    private fun protectWithRecursionStack(petriAtom: PetriAtom, block: () -> Unit) {
        val recursiveStack = cyclicStacks.recursiveStack
        val visitedSet = cyclicStacks.visitedSet

        if (recursiveStack.contains(petriAtom)) {
            return
        }
        if (visitedSet.contains(petriAtom)) {
            return
        }

        visitedSet.add(petriAtom)
        recursiveStack.add(petriAtom)

        block()

        recursiveStack.remove(petriAtom)
    }
}
