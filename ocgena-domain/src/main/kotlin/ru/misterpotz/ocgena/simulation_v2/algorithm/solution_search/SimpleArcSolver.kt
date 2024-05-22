package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.resolveVariables

typealias SolutionAmounts = Map<PlaceWrapper, Int>

class SimpleArcSolver(
    val tokenSlice: TokenSlice,
    private val transitionWrapper: TransitionWrapper
) : Iterable<FullSolution> {
    override fun iterator(): Iterator<FullSolution> {
        val resolvedVariableSpace = tokenSlice.resolveVariables(transitionWrapper.inputArcs)

        val hasEnoughTokens = transitionWrapper.inputArcs.all { arc ->
            val place = arc.fromPlace
            arc.consumptionSpec.strongComplies(tokenSlice.amountAt(place), resolvedVariableSpace)
        }

        if (!hasEnoughTokens){
            return iterator { }
        }

        val solution = transitionWrapper.inputArcs.map { arc ->
            val place = arc.fromPlace
            val tokensToTake = arc.consumptionSpec.tokensShouldTake(tokenSlice.amountAt(place), resolvedVariableSpace)

            Pair(place, tokensToTake)
        }.associateBy({ it.first }) { it.second }

        return iterator {
            yield(FullSolution.Amounts(solution))
        }
    }
}