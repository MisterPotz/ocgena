package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice

typealias SolutionAmounts = Map<PlaceWrapper, Int>

class SimpleArcSolver(
    val tokenSlice: TokenSlice,
    private val transitionWrapper: TransitionWrapper
) : Iterable<FullSolution> {
    // TODO must also consider here the case of variable arcs
    override fun iterator(): Iterator<FullSolution.Amounts> {
        val hasEnoughTokens = transitionWrapper.inputArcs.all { arc ->
            val place = arc.fromPlace
            arc.consumptionSpec.complies(tokenSlice.amountAt(place))
        }
        if (!hasEnoughTokens) return iterator { }

        val solution = transitionWrapper.inputArcs.map { arc ->
            val place = arc.fromPlace
            val tokensToTake = arc.consumptionSpec.tokensShouldTake(tokenSlice.amountAt(place))

            Pair(place, tokensToTake)
        }.associateBy({ it.first }) { it.second }

        return iterator {
            yield(FullSolution.Amounts(solution))
        }
    }
}