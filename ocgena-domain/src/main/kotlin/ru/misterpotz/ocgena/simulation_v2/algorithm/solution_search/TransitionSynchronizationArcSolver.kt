package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.InputArcWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.MultiArcCondition
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IndependentMultiConditionGroup
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice

typealias SolutionTokens = Map<PlaceWrapper, List<TokenWrapper>>

sealed interface IndependentSolution {
    data class MultiConditionGroup(
        val independentMultiConditionGroup: IndependentMultiConditionGroup,
        val combination: Map<PlaceWrapper, List<TokenWrapper>>
    ) : IndependentSolution

    data class Unconditional(
        val place: PlaceWrapper,
        val tokens: List<TokenWrapper>,
    ) : IndependentSolution
}

class TransitionSynchronizationArcSolver(
    val transition: TransitionWrapper,
) {

    fun getSolutionFinderIterable(
        tokenSlice: TokenSlice,
        shuffler: Shuffler
    ): Iterable<SolutionTokens>? {
        val filteredTokenSlice = preCheckSynchronizationHeuristicFiltering(tokenSlice) ?: return null

        val heavySynchronizationSearchCombinator = createGroupSolutionCombinator(
            filteredTokenSlice,
            shuffler,
            transition
        )
        val easyArcsSolutionCombinator = createUnconditionalArcsCombinationSolver(tokenSlice, shuffler)

        val fullCombinator =
            IteratorsCombinator(listOf(heavySynchronizationSearchCombinator, easyArcsSolutionCombinator))

        return IteratorMapper<List<List<IndependentSolution>>, SolutionTokens>(
            fullCombinator
        ) { independentSolutionCombination ->

            val fullSolutionMap = sortedMapOf<PlaceWrapper, List<TokenWrapper>>()

            for (independentSolutionList in independentSolutionCombination) {
                for (partialSolution in independentSolutionList) {
                    when (partialSolution) {
                        is IndependentSolution.MultiConditionGroup -> {
                            require(fullSolutionMap.keys.intersect(partialSolution.combination.keys).isEmpty()) {
                                "solution place-independence condition broken: each combination concerns only its own place, solutions don't intersect places"
                            }
                            fullSolutionMap.putAll(partialSolution.combination)
                        }

                        is IndependentSolution.Unconditional -> {
                            require(fullSolutionMap.keys.contains(partialSolution.place).not()) {
                                "solution place-independence condition broken: each combination concerns only its own place, solutions don't intersect places"
                            }
                            fullSolutionMap[partialSolution.place] = partialSolution.tokens
                        }
                    }
                }
            }
            fullSolutionMap
        }
    }

    private fun createUnconditionalArcsCombinationSolver(
        tokenSlice: TokenSlice,
        shuffler: Shuffler
    ): Iterable<List<IndependentSolution.Unconditional>> {
        val allUnconditionalCombinationsIterator = transition.unconditionalArcs.map { unconditionalArc ->
            val tokenIndicesRange = unconditionalArc.fromPlace.tokenRangeAt(tokenSlice)
            val tokensTotal = tokenSlice.tokensAt(unconditionalArc.fromPlace).size

            val tokensToTake = unconditionalArc.consumptionSpec.tokensShouldTake(tokensTotal)

            val combination = CombinationIterable(
                shuffler.makeShuffled(tokenIndicesRange),
                combinationSize = tokensToTake
            )

            IteratorMapper(combination) { combination ->
                IndependentSolution.Unconditional(
                    unconditionalArc.fromPlace,
                    tokenSlice.tokensAt(unconditionalArc.fromPlace).selectTokens(combination)
                )
            }
        }
        return IteratorsCombinator(allUnconditionalCombinationsIterator)
    }

    private fun preCheckSynchronizationHeuristicFiltering(tokenSlice: TokenSlice): TokenSlice? {
        val preliminaryDumbCheck = transition.inputArcs.all { arc ->
            arc.consumptionSpec.complies(tokenSlice.amountAt(arc.fromPlace))
        }

        if (!preliminaryDumbCheck) {
            return null
        }

        val filteredTokenSlice = tokenSlice.filterTokensInPlaces(transition.prePlaces) { token, place ->
            token.visitedTransitions.containsAll(transition.inputArcBy(place.placeId).syncTransitions)
        }

        val secondPreliminaryDumbCheck = transition.inputArcs.all { arc ->
            arc.consumptionSpec.complies(tokenSlice.amountAt(arc.fromPlace))
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
    ): Iterator<IndependentSolution.MultiConditionGroup> = iterator {
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
                val completeSolution = makeCompleteSolution(
                    hypotheticCombination,
                    placeToIndex,
                    indexToPlace,
                    filteredTokenSlice,
                    independentMultiConditionGroup,
                    conditionSatisfactoryEntries
                )

                yield(
                    IndependentSolution.MultiConditionGroup(
                        independentMultiConditionGroup,
                        indicesToTokenLists(completeSolution, placeToIndex, filteredTokenSlice)
                    )
                )
            }
        }
    }

    private fun makeCompleteSolution(
        potentiallyUncompleteCombination: List<List<Int>>,
        placeToIndex: Map<PlaceWrapper, Int>,
        indexToPlace: Map<Int, PlaceWrapper>,
        filteredTokenSlice: TokenSlice,
        independentMultiConditionGroup: IndependentMultiConditionGroup,
        conditionRequiredEntries: Map<MultiArcCondition, HashSet<Long>>
    ): List<List<Int>> {
        val newIndicesToAppend = mutableMapOf<PlaceWrapper, List<Int>>()

        for (inputArc in independentMultiConditionGroup.relatedInputArcs) {
            if (inputArc.consumptionSpec.isUnconstrained()) {
                val fromPlace = inputArc.fromPlace
                val allPlaceTokens = filteredTokenSlice.tokensAt(fromPlace)
                val alreadyGoodIndices = potentiallyUncompleteCombination[placeToIndex[fromPlace]!!]

                val newIndicesToAppendBuffer = mutableListOf<Int>()

                for (arcCondition in inputArc.underConditions) {
                    val requiredEntries = conditionRequiredEntries[arcCondition]!!

                    for ((index, token) in allPlaceTokens.withIndex()) {
                        if (index !in alreadyGoodIndices && token.participatedInAll(requiredEntries)) {
                            newIndicesToAppendBuffer.add(index)
                        }
                    }
                }
                if (newIndicesToAppendBuffer.isNotEmpty()) {
                    newIndicesToAppend[fromPlace] = newIndicesToAppendBuffer
                }
            }
        }
        if (newIndicesToAppend.isEmpty()) return potentiallyUncompleteCombination

        return potentiallyUncompleteCombination.mapIndexed { index, ints ->
            buildList {
                addAll(ints)
                addAll(newIndicesToAppend[indexToPlace[index]] ?: emptyList())
            }
        }
    }

    private fun createGroupSolutionCombinator(
        filteredTokenSlice: TokenSlice,
        shuffler: Shuffler,
        transition: TransitionWrapper,
    ): Iterable<List<IndependentSolution.MultiConditionGroup>> {
        val independentMultiConditionGroups = transition.independentMultiArcConditions

        val solutionSearchIterables = independentMultiConditionGroups.map {
            object : Iterable<IndependentSolution.MultiConditionGroup> {
                override fun iterator(): Iterator<IndependentSolution.MultiConditionGroup> {
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