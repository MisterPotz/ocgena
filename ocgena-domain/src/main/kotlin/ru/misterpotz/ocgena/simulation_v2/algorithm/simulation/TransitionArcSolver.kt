package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.ocgena.simulation_v2.entities.InputArcWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.Places
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IntersectingMultiArcConditions
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.utils.Randomizer


class TransitionArcSolver(
    val transition: TransitionWrapper,
) {

    suspend fun getSolutions(tokenSlice: TokenSlice, randomizer: Randomizer): List<ArcSolver.LazySolution> {
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


        for (intersectingCondition in transition.intersectingMultiArcConditions) {
            val sortedArcs = intersectingCondition.conditionArcSortedByStrongestDescending()

            val combinationsIterator = intersectingCondition.makeRandomCombinationIterator(
                filteredTokenSlice,
                randomizer = randomizer
            )

            while (combinationsIterator.hasNext()) {
                val hypotheticCombination = combinationsIterator.next()

                checkCombination(filteredTokenSlice, intersectingCondition, sortedArcs, hypotheticCombination)

            }

            combinationsIterator.forEach { combination ->

            }
        }

        return listOf()
    }

    private fun checkCombination(
        tokenSlice: TokenSlice,
        intersectingMultiArcConditions: IntersectingMultiArcConditions,
        sortedArcWrappers: List<InputArcWrapper>,
        indicesCombination: List<List<Int>>
    ): Boolean {
        require(sortedArcWrappers.size == indicesCombination.size)

        val sharedEntriesPerPlace = sortedArcWrappers.mapIndexed { index, inputArc ->
            val tokenIndices = indicesCombination[index]
            val selectedTokens = tokenSlice.tokensAt(inputArc.fromPlace).selectTokens(tokenIndices)

            selectedTokens.sharedTransitionEntries(inputArc.underConditions.map { it.syncTarget })
        }

        if (sharedEntriesPerPlace.any { it.isEmpty() }) {
            return false
        }

        // cross-place by condition check
        intersectingMultiArcConditions.conditions

    }

    fun Collection<TokenWrapper>.selectTokens(indices: List<Int>): List<TokenWrapper> {
        return this.withIndex().mapIndexedNotNull { index, tokenWrapper ->
            if (index in indices) {
                tokenWrapper.value
            } else {
                null
            }
        }
    }

    fun List<TokenWrapper>.sharedTransitionEntries(includeEntriesOf: List<TransitionWrapper>): HashSet<Long> {
        val sharedTransitionEntries = hashSetOf<Long>().apply {
            addAll(get(0).allParticipatedTransitionEntries)
        }

        for (i in 1..<size) {
            val tokenI = get(i)
            if (sharedTransitionEntries.isEmpty()) {
                return sharedTransitionEntries
            }
            sharedTransitionEntries.retailAllShared(tokenI)
        }

        sharedTransitionEntries.filterContainedInAny(includeEntriesOf)
        return sharedTransitionEntries
    }

    fun HashSet<Long>.filterContainedInAny(transitionHistories: List<TransitionWrapper>) {
        val iterator = iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (transitionHistories.any { next in it.transitionHistory.allLogIndices }) {
                continue
            } else {
                iterator.remove()
            }
        }
    }

    fun HashSet<Long>.retailAllShared(tokenWrapper: TokenWrapper) {
        this.retainAll(tokenWrapper.allParticipatedTransitionEntries)
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