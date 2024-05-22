package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IndependentMultiConditionGroup
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.resolveVariables

class TransitionSyncV2MinimalIndepdendentsSolutionFinder(val transition: TransitionWrapper, val shuffler: Shuffler) {

    fun List<InputArcWrapper>.makeIndicesOfPlacesForCondition(condition: MultiArcCondition): List<Int> {
        val arcs = condition.arcs.ref
        val combinationIndices = fold(mutableListOf<Int>()) { acc, arc ->
            arcs.indexOfFirst { it == arc }.takeIf { it >= 0 }?.let {
                acc.add(it)
            }
            acc
        }.sorted()
        return combinationIndices
    }

    private fun findCommonEntryForCondition(
        tokens: Iterable<TokenWrapper>,
        condition: MultiArcCondition
    ): Set<TokenWrapper>? {
        for (entry in condition.syncTarget.transitionHistory.entries()) {
            if (tokens.all { it in entry }) {
                return entry
            }
        }

        return null
    }

    private fun findCommonEntriesForIndependent(
        independentGroup: IndependentMultiConditionGroup,
        tokenCombination: List<List<TokenWrapper>>,
    ): Map<MultiArcCondition, Set<TokenWrapper>> {
        val sortedArcs = independentGroup.conditionArcSortedByStrongestDescending()

        val conditionToEntries = mutableMapOf<MultiArcCondition, Set<TokenWrapper>>()

        independentGroup.conditions.forEach { condition ->
            val combinationIndices = sortedArcs.makeIndicesOfPlacesForCondition(condition)
            val commonEntry = findCommonEntryForCondition(
                FilteredIterator(combinationIndices, tokenCombination),
                condition
            )
            conditionToEntries[condition] = requireNotNull(commonEntry) {
                "at this stage there already must be a guaranteed common entry"
            }
        }
        return conditionToEntries
    }


    fun haveCommonEntryForAll(tokens: Iterable<TokenWrapper>, transitions: List<TransitionWrapper>): Boolean {
        for (transition in transitions) {
            var hasCommonEntryForTransition = false
            for (entry in transition.transitionHistory.entries()) {
                if (tokens.all { it in entry }) {
                    hasCommonEntryForTransition = true
                    break;
                }
            }
            if (!hasCommonEntryForTransition) {
                return false
            }
        }

        return true
    }

    fun usePresolutionForMinimal(
        presolution: Map<IndependentMultiConditionGroup, IndependentGroupSolution>
    ): TokenSlice {

        val solutionTokenSlice = SimpleTokenSlice.build {
            transition.prePlaces.forEach { addRelatedPlace(it) }
            for ((independentGroup, combination) in presolution) {
                val sortedArcs = independentGroup.conditionArcSortedByStrongestDescending()
                combination.combination.zip(sortedArcs) { combination, sortedArc ->
                    addTokens(sortedArc.fromPlace, combination)
                }
            }
        }
        return solutionTokenSlice
    }

    data class IndependentGroupSolution(
        val combination: List<List<TokenWrapper>>,
        val sharedEntries: Map<MultiArcCondition, Set<TokenWrapper>>
    )

    fun findFirstSolution(tokenSlice: TokenSlice):
            Map<IndependentMultiConditionGroup, IndependentGroupSolution>? {
        if (!transition.model.isSynchronizedMode()) {
            return emptyMap()
        }
        val independentCombinationIterator =
            transition.independentMultiArcConditions.map {
                val sortedArcs = it.conditionArcSortedByStrongestDescending()
                Triple(it, sortedArcs, makeIndependentGroupTokensIterator(tokenSlice, sortedArcs, shuffler = shuffler))
            }

        val combinationPerIndependentGroup = mutableMapOf<IndependentMultiConditionGroup, List<List<TokenWrapper>>>()

        for ((independentGroup, sortedArcs, iterator) in independentCombinationIterator) {
            for (combination in iterator) {
                val allConditionsSatisfied = independentGroup.conditions.all { condition ->
                    val combinationIndices = sortedArcs.makeIndicesOfPlacesForCondition(condition)
                    haveCommonEntryForAll(
                        FilteredIterator(combinationIndices, combination),
                        transitions = listOf(condition.syncTarget)
                    )
                }
                if (allConditionsSatisfied) {
                    // if we came here it means found shared entries for given places for all conditions
                    // adding the combinations
                    combinationPerIndependentGroup[independentGroup] = combination
                    break;
                }
            }
            // not found such token combination that independent group is satisfied
            if (!combinationPerIndependentGroup.contains(independentGroup)) {
                return null
            }
        }
        return combinationPerIndependentGroup.mapValues {
            IndependentGroupSolution(
                combination = it.value,
                sharedEntries = findCommonEntriesForIndependent(it.key, it.value)
            )
        }
    }

    private fun makeIndependentGroupTokensIterator(
        tokenSlice: TokenSlice,
        sortedArcs: List<InputArcWrapper>,
        shuffler: Shuffler
    ): Iterable<List<List<TokenWrapper>>> {
        val combination = IteratorsCombinator(
            sortedArcs.map { makePlaceTokensIteratorByArcRequirement(tokenSlice, it, shuffler = shuffler) }
        )
        return combination
    }

    private fun makePlaceTokensIteratorByArcRequirement(
        tokenSlice: TokenSlice,
        inputArcWrapper: InputArcWrapper,
        shuffler: Shuffler
    ): CombinationIterable<TokenWrapper> {
        val requirement = inputArcWrapper.consumptionSpec.tokenRequirement()
        val tokens = tokenSlice.tokensAt(inputArcWrapper.fromPlace)

        val shuffled = tokens.buildFromIndices(shuffler.makeShuffled(tokens.indices))
        return CombinationIterable(shuffled, requirement)
    }


    fun preCheckSynchronizationHeuristicFiltering(tokenSlice: TokenSlice): TokenSlice? {
        val preliminaryVariablesSpace = tokenSlice.resolveVariables(transition.inputArcs)

        val preliminaryDumbCheck = transition.inputArcsSortedByRequiredTokens.all { arc ->
            if (transition.model.isSynchronizedMode()) {
                arc.consumptionSpec.weakComplies(tokenSlice.amountAt(arc.fromPlace))
            } else {
                arc.consumptionSpec.strongComplies(tokenSlice.amountAt(arc.fromPlace), preliminaryVariablesSpace)
            }
        }

        if (!preliminaryDumbCheck) {
            return null
        }

        // correctly build per arc

        val filteredTokenSlice = SimpleTokenSlice.build {
            for (inputArc in transition.inputArcs) {
                val place = inputArc.fromPlace
                val tokens = tokenSlice.tokensAt(place)


                val updatedTokens = if (inputArc.syncTransitions.isNotEmpty()) {
                    tokens.filter {
                        it.visitedTransitions.containsAll(inputArc.syncTransitions)
                    }
                } else {
                    tokens
                }
                val updatedAmount = if (inputArc.syncTransitions.isNotEmpty()) {
                    updatedTokens.size
                } else {
                    tokenSlice.amountAt(place)
                }

                addTokens(place, updatedTokens)
                addAmount(place, updatedAmount)
            }
        }

        val secondPreliminaryDumbCheck = transition.inputArcsSortedByRequiredTokens.all { arc ->
            arc.consumptionSpec.weakComplies(filteredTokenSlice.amountAt(arc.fromPlace))
        }

        if (!secondPreliminaryDumbCheck) {
            return null
        }

        // set correct amounts for
        return filteredTokenSlice
    }
}