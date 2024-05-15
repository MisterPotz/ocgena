package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IndependentMultiConditionGroup
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.resolveVariables
import java.util.*
import kotlin.collections.HashSet

sealed interface FullSolution {
    data class Tokens(val solutionTokens: SolutionTokens) : FullSolution
    data class Amounts(val solutionAmounts: SolutionAmounts) : FullSolution

    fun toTokenSlice(): TokenSlice {
        return when (this) {
            is Amounts -> {
                SimpleTokenSlice.ofAmounts(solutionAmounts)
            }

            is Tokens -> {
                SimpleTokenSlice.of(solutionTokens)
            }
        }
    }
}

typealias SolutionTokens = Map<PlaceWrapper, List<TokenWrapper>>

sealed interface PartialSolutionCandidate {
    data class MultiConditionGroup(
        val independentMultiConditionGroup: IndependentMultiConditionGroup,
        val conditionSatisfactoryEntries: Map<MultiArcCondition, HashSet<Long>>,
        val potentiallyUncompleteCombination: Map<PlaceWrapper, List<TokenWrapper>>,
    ) : PartialSolutionCandidate

    data class Unconditional(
        val place: PlaceWrapper,
        val tokens: List<TokenWrapper>,
    ) : PartialSolutionCandidate

}

class TransitionSynchronizationArcSolver(
    val transition: TransitionWrapper,
) {

    fun getSolutionFinderIterable(
        tokenSlice: TokenSlice,
        shuffler: Shuffler
    ): Iterable<FullSolution>? {
        val filteredTokenSlice = preCheckSynchronizationHeuristicFiltering(tokenSlice) ?: return null

        val heavySynchronizationSearchCombinator = createGroupSolutionCombinator(
            filteredTokenSlice,
            shuffler,
            transition
        )
        val easyArcsSolutionCombinator = createUnconditionalArcsCombinationSolutionSuggester(tokenSlice, shuffler)

        val fullCombinator =
            IteratorsCombinator(
                listOf(
                    heavySynchronizationSearchCombinator,
                    easyArcsSolutionCombinator
                )
            )

        return IteratorMapper(
            fullCombinator
        ) { independentSolutionCombination ->
            completeSolutionFromPartials(independentSolutionCombination.flatten(), filteredTokenSlice, shuffler)
        }
    }

    private fun completeSolutionFromPartials(
        partials: List<PartialSolutionCandidate>,
        tokenSlice: TokenSlice,
        shuffler: Shuffler,
    ): FullSolution {
        val fullSolutionMap = sortedMapOf<PlaceWrapper, List<TokenWrapper>>()
        val independentGroupSolutionTransitionEntries =
            mutableMapOf<IndependentMultiConditionGroup, Map<MultiArcCondition, HashSet<Long>>>()

        for (partial in partials) {
            when (partial) {
                is PartialSolutionCandidate.MultiConditionGroup -> {
                    require(
                        fullSolutionMap.keys.intersect(partial.potentiallyUncompleteCombination.keys)
                            .isEmpty()
                    ) {
                        "solution place-independence condition broken: each combination concerns only its own place, solutions don't intersect places"
                    }
                    fullSolutionMap.putAll(partial.potentiallyUncompleteCombination)

                    independentGroupSolutionTransitionEntries[partial.independentMultiConditionGroup] =
                        partial.conditionSatisfactoryEntries
                }

                is PartialSolutionCandidate.Unconditional -> {
                    require(fullSolutionMap.keys.contains(partial.place).not()) {
                        "solution place-independence condition broken: each combination concerns only its own place, solutions don't intersect places"
                    }
                    fullSolutionMap[partial.place] = partial.tokens
                }
            }
        }

        val completeSolution = makeCompleteSolution(
            fullSolutionMap,
            independentGroupSolutionTransitionEntries,
            tokenSlice,
            shuffler = shuffler
        )
        return FullSolution.Tokens(completeSolution)
    }

    private fun createUnconditionalArcsCombinationSolutionSuggester(
        tokenSlice: TokenSlice,
        shuffler: Shuffler
    ): Iterable<List<PartialSolutionCandidate.Unconditional>> {
        val allUnconditionalCombinationsIterator = transition.unconditionalInputArcs.map { unconditionalArc ->
            val tokenIndicesRange = unconditionalArc.fromPlace.tokenRangeAt(tokenSlice)
            val tokensToTake = unconditionalArc.consumptionSpec.tokenRequirement()

            val combination = CombinationIterable(
                shuffler.makeShuffled(tokenIndicesRange),
                combinationSize = tokensToTake
            )

            IteratorMapper(combination) { combination ->
                PartialSolutionCandidate.Unconditional(
                    unconditionalArc.fromPlace,
                    tokenSlice.tokensAt(unconditionalArc.fromPlace).selectTokens(combination)
                )
            }
        }
        return IteratorsCombinator(allUnconditionalCombinationsIterator)
    }

    private fun preCheckSynchronizationHeuristicFiltering(tokenSlice: TokenSlice): TokenSlice? {
        val preliminaryVariablesSpace = tokenSlice.resolveVariables(transition.inputArcs)

        val preliminaryDumbCheck = transition.inputArcsSortedByRequiredTokens.all { arc ->
            if (transition.model.isSynchronizedMode()) {
                arc.consumptionSpec.weakComplies(tokenSlice.amountAt(arc.fromPlace))
            } else {
                arc.consumptionSpec.strongComplies(tokenSlice.amountAt(arc.fromPlace), preliminaryVariablesSpace)
            }
        }

        if (!transition.model.isSynchronizedMode()) {
            return tokenSlice
        }

        if (!preliminaryDumbCheck) {
            return null
        }

        val filteredTokenSlice = tokenSlice.filterTokensInPlaces(transition.prePlaces) { token, place ->
            val inputArc = transition.inputArcBy(place.placeId)
            if (inputArc.syncTransitions.isNotEmpty()) {
                token.visitedTransitions.containsAll(inputArc.syncTransitions)
            } else {
                true
            }
        }

        val secondPreliminaryDumbCheck =
            transition.inputArcsSortedByRequiredTokens.all { arc ->
                arc.consumptionSpec.weakComplies(tokenSlice.amountAt(arc.fromPlace))
            }

        if (!secondPreliminaryDumbCheck) {
            return null
        }
        return filteredTokenSlice
    }

    private fun getIndependentMultiArcConditionSolutionIterator(
        filteredTokenSlice: TokenSlice,
        shuffler: Shuffler,
        independentMultiConditionGroup: IndependentMultiConditionGroup
    ): Iterator<PartialSolutionCandidate.MultiConditionGroup> = iterator {
        val sortedArcs = independentMultiConditionGroup.conditionArcSortedByStrongestDescending()

        val (placeToIndex, indexToPlace, combinationsIterator) = independentMultiConditionGroup.makeRandomCombinationIterator(
            filteredTokenSlice,
            shuffler = shuffler
        )

        while (combinationsIterator.hasNext()) {
            val hypotheticCombination = combinationsIterator.next()

            val perPlaceSharedEntries = makePerPlaceSharedTransitionEntries(
                sortedArcs,
                filteredTokenSlice,
                placeToIndex,
                indicesCombination = hypotheticCombination
            )

            val conditionSatisfactoryEntries =
                getConditionSatisfactoryEntries(
                    independentMultiConditionGroup,
                    sortedArcs,
                    hypotheticCombination,
                    perPlaceSharedEntries
                )

            if (conditionSatisfactoryEntries.isNotEmpty()) {
                yield(
                    PartialSolutionCandidate.MultiConditionGroup(
                        independentMultiConditionGroup,
                        conditionSatisfactoryEntries,
                        potentiallyUncompleteCombination = indicesToTokenLists(
                            hypotheticCombination,
                            placeToIndex,
                            filteredTokenSlice
                        ),
                    )
                )
            }
        }
    }

    private fun makeCompleteSolution(
        potentiallyUncompleteSolution: SortedMap<PlaceWrapper, List<TokenWrapper>>,
        conditionRequiredEntries: Map<IndependentMultiConditionGroup, Map<MultiArcCondition, HashSet<Long>>>,
        filteredTokenSlice: TokenSlice,
        shuffler: Shuffler,
    ): Map<PlaceWrapper, List<TokenWrapper>> {
        val placeToApplicableNewTokens = mutableMapOf<PlaceWrapper, MutableList<TokenWrapper>>()

        val resolvedVariablesSpace = filteredTokenSlice.resolveVariables(transition.inputArcs)
        val bufferVariablesSpace = resolvedVariablesSpace.copy()

        // найти для каждой арки потенциальный максимальный набор подходящих токенов
        for (inputArc in transition.inputArcs) {
            if (inputArc.consumptionSpec.isUnconstrained()) {
                val fromPlace = inputArc.fromPlace
                val allPlaceTokens = filteredTokenSlice.tokensAt(fromPlace)
                val alreadyGoodTokens = potentiallyUncompleteSolution[fromPlace]!!

                for (arcCondition in inputArc.underConditions) {
                    val requiredEntries = conditionRequiredEntries[inputArc.independentGroup]!![arcCondition]!!

                    for (token in allPlaceTokens) {
                        if (token !in alreadyGoodTokens && token.participatedInAll(requiredEntries)) {
                            placeToApplicableNewTokens.getOrPut(fromPlace) { mutableListOf() }.add(token)
                        }
                    }
                }
            }
        }

        // попытаться из найденных подходящих токенов выбрать под большее покрытие арок с Lomazova variable arcs
        for ((varArc, dependent) in transition.lomazovaVariableArcsToDependent) {
            // try to gather more tokens for var arc
            val fromPlace = varArc.fromPlace

            if (placeToApplicableNewTokens[fromPlace] == null ||
                placeToApplicableNewTokens[fromPlace]!!.isEmpty() ||
                dependent.isEmpty()
            ) {
                // leaving all new tokens per var arc as its
                continue
            }
            val variableName = varArc.consumptionSpec.castVar().variableName
            val newTokensGoodForVarArc = placeToApplicableNewTokens[fromPlace]!!

            var newTokensAmountForVarArcSolution = 0
            for (index in newTokensGoodForVarArc.indices) {
                bufferVariablesSpace.copyFrom(resolvedVariablesSpace)
                val initialVariableValue = bufferVariablesSpace.getVariable(variableName)
                bufferVariablesSpace.setVariable(variableName, initialVariableValue + index + 1)

                val allDependendsArcsSatisfied = dependent.all { dependentArc ->
                    val tokensAtDependentArcPlace = placeToApplicableNewTokens[dependentArc.fromPlace]
                    if (tokensAtDependentArcPlace == null) {
                        false
                    } else {
                        dependentArc.consumptionSpec.strongComplies(
                            tokensAtDependentArcPlace.size,
                            bufferVariablesSpace
                        )
                    }
                }
                if (allDependendsArcsSatisfied) {
                    newTokensAmountForVarArcSolution = index + 1
                } else {
                    break
                }
            }
            // leaving current var arc solution for the variable as it is
            if (newTokensAmountForVarArcSolution == 0) continue

            // updating the variable space for this variable
            resolvedVariablesSpace.setVariable(
                variableName,
                resolvedVariablesSpace.getVariable(variableName) + newTokensAmountForVarArcSolution
            )
            val newTokensToAddToVarArcSolution =
                placeToApplicableNewTokens[fromPlace]!!.selectTokens(
                    shuffler.makeShuffled(newTokensGoodForVarArc.indices)
                        .take(newTokensAmountForVarArcSolution)
                )
                    .toMutableList()

            placeToApplicableNewTokens[fromPlace] = newTokensToAddToVarArcSolution

            for (dependentArc in dependent) {
                val dependentArcPlace = dependentArc.fromPlace

                val tokensAtDependentArcPlace = placeToApplicableNewTokens[dependentArcPlace]!!
                // for each dependent place must
                val tokensToTake = dependentArc.consumptionSpec.tokensShouldTake(
                    tokensAtDependentArcPlace.size,
                    resolvedVariablesSpace
                )

                val newTokensToAddToDependentArcSolution =
                    placeToApplicableNewTokens[dependentArcPlace]!!.selectTokens(
                        shuffler.makeShuffled(tokensAtDependentArcPlace.indices)
                            .take(tokensToTake)
                    )
                        .toMutableList()

                placeToApplicableNewTokens[dependentArcPlace] = newTokensToAddToDependentArcSolution
            }
        }

        if (placeToApplicableNewTokens.isEmpty()) return potentiallyUncompleteSolution

        return buildMap {
            potentiallyUncompleteSolution.map { (place, tokens) ->
                put(place, buildList {
                    addAll(tokens)
                    addAll(placeToApplicableNewTokens[place]!!)
                })
            }
        }
    }

    private fun createGroupSolutionCombinator(
        filteredTokenSlice: TokenSlice,
        shuffler: Shuffler,
        transition: TransitionWrapper,
    ): Iterable<List<PartialSolutionCandidate.MultiConditionGroup>> {
        if (transition.model.isSynchronizedMode().not()) {
            return emptyList()
        }
        val independentMultiConditionGroups = transition.independentMultiArcConditions

        val solutionSearchIterables = independentMultiConditionGroups.map {
            object : Iterable<PartialSolutionCandidate.MultiConditionGroup> {
                override fun iterator(): Iterator<PartialSolutionCandidate.MultiConditionGroup> {
                    return getIndependentMultiArcConditionSolutionIterator(
                        filteredTokenSlice,
                        shuffler,
                        it
                    )
                }
            }
        }

        return IteratorsCombinator(solutionSearchIterables)
    }

    private fun indicesToTokenLists(
        indices: List<List<Int>>,
        placeToIndex: Map<PlaceWrapper, Int>,
        tokenSlice: TokenSlice,
    ): Map<PlaceWrapper, List<TokenWrapper>> {
        return placeToIndex.map { (place, index) ->
            val placeIndices = indices[index]

            Pair(place, place.getTokens(tokenSlice).selectTokens(placeIndices))
        }.associateBy({ it.first }, { it.second })
    }

    private fun makePerPlaceSharedTransitionEntries(
        sortedArcWrappers: List<InputArcWrapper>,
        tokenSlice: TokenSlice,
        placeToIndex: Map<PlaceWrapper, Int>,
        indicesCombination: List<List<Int>>
    ): Map<PlaceWrapper, Pair<List<TokenWrapper>, HashSet<Long>>> {
        return sortedArcWrappers.map { inputArc ->
            val tokenIndices = indicesCombination[placeToIndex[inputArc.fromPlace]!!]
            val selectedTokens = tokenSlice.tokensAt(inputArc.fromPlace).selectTokens(tokenIndices)
            Triple(
                inputArc.fromPlace,
                selectedTokens,
                selectedTokens.sharedTransitionEntries(inputArc.underConditions.map { it.syncTarget })
            )
        }.associateBy({ it.first }, { Pair(it.second, it.third) })
    }

    private fun getConditionSatisfactoryEntries(
        independentMultiConditionGroup: IndependentMultiConditionGroup,
        sortedArcWrappers: List<InputArcWrapper>,
        indicesCombination: List<List<Int>>,
        perPlaceSharedEntries: Map<PlaceWrapper, Pair<List<TokenWrapper>, HashSet<Long>>>
    ): Map<MultiArcCondition, HashSet<Long>> {
        require(sortedArcWrappers.size == indicesCombination.size)

        if (perPlaceSharedEntries.any { it.value.second.isEmpty() }) {
            return emptyMap()
        }

        val conditionToSatisfactoryEntries = mutableMapOf<MultiArcCondition, HashSet<Long>>()

        // index to multi arc condition satisfying
        // cross-place by condition check,
        // for each group of intersecting conditions
        val allSatisfied = independentMultiConditionGroup.conditions.all { condition ->
            val conditionSatisfactoryEntries = findConditionSatisfactoryTransionEntries(
                multiArcCondition = condition,
                placesToTokensAndCommonEntries = perPlaceSharedEntries
            )

            conditionToSatisfactoryEntries[condition] = conditionSatisfactoryEntries
            conditionSatisfactoryEntries.isNotEmpty()
        }

        if (!allSatisfied) return emptyMap()

        return conditionToSatisfactoryEntries
    }

    private fun findConditionSatisfactoryTransionEntries(
        multiArcCondition: MultiArcCondition,
        placesToTokensAndCommonEntries: Map<PlaceWrapper, Pair<List<TokenWrapper>, HashSet<Long>>>,
    ): HashSet<Long> {
        // need to confirm hashes that tokens at fromPlaces are not empty when intersected
        val common = hashSetOf<Long>()
        val placeIterator = multiArcCondition.fromPlaces.iterator()
        val syncTransition = listOf(multiArcCondition.syncTarget)

        if (!placeIterator.hasNext()) return hashSetOf()

        val firstPlace = placeIterator.next()

        placesToTokensAndCommonEntries[firstPlace]!!.let { (_, entriesHash) ->
            common.addAll(entriesHash)
            common.filterContainedInAny(syncTransition)
        }

        placeIterator.forEach { place ->
            val (_, entriesHash) = placesToTokensAndCommonEntries[place]!!
            common.retainAll(entriesHash)
            common.filterContainedInAny(syncTransition)

            if (common.isEmpty()) {
                return common
            }
        }

        return common
    }

    private fun Collection<TokenWrapper>.selectTokens(indices: List<Int>): List<TokenWrapper> {
        return this.withIndex().mapIndexedNotNull { index, tokenWrapper ->
            if (index in indices) {
                tokenWrapper.value
            } else {
                null
            }
        }
    }

    private fun List<TokenWrapper>.sharedTransitionEntries(includeEntriesOf: List<TransitionWrapper>): HashSet<Long> {
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

    private fun HashSet<Long>.filterContainedInAny(transitionHistories: List<TransitionWrapper>) {
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

    private fun HashSet<Long>.retailAllShared(tokenWrapper: TokenWrapper) {
        this.retainAll(tokenWrapper.allParticipatedTransitionEntries)
    }

    class IteratorMapper<T, R>(
        val iterable: Iterable<T>, val mapper: (T) -> R
    ) : Iterable<R> {
        override fun iterator(): Iterator<R> {
            val innerIterator = iterable.iterator()

            return iterator {
                while (innerIterator.hasNext()) {
                    val nextValue = innerIterator.next()
                    val newValue = mapper(nextValue)
                    yield(newValue)
                }
            }
        }
    }

    class IteratorsCombinator<T>(
        private val iterables: List<Iterable<T>>
    ) : Iterable<List<T>> {
        private suspend fun SequenceScope<List<T>>.generateCombinationsDepthFirst(
            level: Int,
            levelIterables: List<Iterable<T>>,
            current: MutableList<T?>
        ) {
            if (level == iterables.size) {
                yield(current.mapNotNull { it })
                return
            }
            val thisLevelIterable = levelIterables[level].iterator()

            for (i in thisLevelIterable) {
                current[level] = i
                generateCombinationsDepthFirst(level + 1, levelIterables, current)
            }
        }

        override fun iterator(): Iterator<List<T>> {
            val dumbIterator = iterator {
                generateCombinationsDepthFirst(
                    0,
                    iterables,
                    MutableList(iterables.size) { null }
                )
            }
            return object : Iterator<List<T>> {
                override fun hasNext(): Boolean {
                    return dumbIterator.hasNext()
                }

                override fun next(): List<T> {
                    return dumbIterator.next()
                }
            }
        }
    }
}