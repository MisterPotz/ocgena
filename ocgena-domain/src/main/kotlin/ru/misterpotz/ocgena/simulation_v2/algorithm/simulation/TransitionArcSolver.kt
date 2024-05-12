package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice


class TransitionArcSolver(
    val transition: TransitionWrapper,
) {

    suspend fun getSolutions(tokenSlice: TokenSlice): List<ArcSolver.LazySolution> {
        // need to consider the conditions
        // filtering it is

        val preliminaryDumbCheck = transition.inputArcs.all { arc ->
            arc.consumptionSpec.complies(tokenSlice.amountAt(arc.fromPlace))
        }

        if (!preliminaryDumbCheck) {
            return emptyList()
        }

        // visited all required sync transitions
        val filteredTokenSlice = tokenSlice.filterTokensInPlaces(transition.prePlaces) { token, place ->
            token.visitedTransitions.containsAll(transition.inputArcBy(place.placeId).syncTransitions)
        }

        val secondPreliminaryDumbCheck = transition.inputArcs.all { arc ->
            arc.consumptionSpec.complies(tokenSlice.amountAt(arc.fromPlace))
        }
        if (!secondPreliminaryDumbCheck) {
            return emptyList()
        }

        for (intersectingCondition in transition.intersectingMultiArcConditions.sortedByDescending { it.biggestArcConditionAmount }) {
            for (strongArc in intersectingCondition.strongestConditionArcs) {
                val fromPlace = strongArc.fromPlace

                filteredTokenSlice.modifyTokensAt(fromPlace) { tokens ->
                    for (token in tokens) {
                        token.prepareBuffer()

                        // try to find a match from other place of the condition of strongest arc
                        for (relatedArc in strongArc.allAssociatedArcs) {
                            val relatedArcTokens = filteredTokenSlice.tokensAt(relatedArc.fromPlace)
                            // try find a relative token



                            token.buffer.put(token.participatedTransitionIndices)
                        }
                        if (token.participatedTransitionIndices.)
                    }
                }
            }
        }


    }

    class ArcGroupCondition(
        val transition: TransitionWrapper,
        val fromPlaces: Places,

        ) {
        fun isSatisfied(tokenSlice: TokenSlice) {

        }

//        fun get
    }
}