package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.Shuffler
import ru.misterpotz.ocgena.simulation_v2.entities.InputArcWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.MultiArcCondition
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IndependentMultiConditionGroup
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.utils.step

typealias SolutionTokens = Map<PlaceWrapper, List<TokenWrapper>>

typealias IndependentConditionsSolution = Pair<IndependentMultiConditionGroup, Map<PlaceWrapper, List<TokenWrapper>>>

class TransitionArcSolver(
    val transition: TransitionWrapper,
) {

    fun getSolutionFinderIterable(
        tokenSlice: TokenSlice,
        shuffler: Shuffler
    ): Iterable<SolutionTokens>? {
        val filteredTokenSlice = preCheckHeuristicFiltering(tokenSlice) ?: return null

        val combinator = createGroupSolutionCombinator(
            filteredTokenSlice,
            shuffler,
            transition
        )
        return IteratorMapper<List<IndependentConditionsSolution>, SolutionTokens>(combinator) { groupSolutionCombination ->
            groupSolutionCombination
                .fold(sortedMapOf()) { acc: MutableMap<PlaceWrapper, List<TokenWrapper>>,
                                      (group, placeToTokens) ->
                    for ((place, token) in placeToTokens) {
                        require(place !in acc) {
                            "at this stage there should be totally no intersections between places of solutions"
                        }
                        acc[place] = token
                    }
                    acc
                }
        }
    }

    fun findAnyExistingSolution(
        tokenSlice: TokenSlice,
        shuffler: Shuffler
    ): SolutionTokens? {
        val filteredTokenSlice = preCheckHeuristicFiltering(tokenSlice) ?: return null

        // finding solution for each independent arc group
        val foundCombinations = transition.independentMultiArcConditions
            .fold(
                mutableListOf<Pair<IndependentMultiConditionGroup, Map<PlaceWrapper, List<TokenWrapper>>>?>()
            ) { acc, independentMultiConditionGroup ->
                if (acc.contains(null)) return@fold acc

                val sortedArcs = independentMultiConditionGroup.conditionArcSortedByStrongestDescending()

                val (placeToIndex, combinationsIterator) = independentMultiConditionGroup.makeRandomCombinationIterator(
                    filteredTokenSlice,
                    shuffler = shuffler
                )

                while (combinationsIterator.hasNext()) {
                    val hypotheticCombination = combinationsIterator.next()

                    val combinationIsGood =
                        checkCombination(
                            filteredTokenSlice,
                            independentMultiConditionGroup,
                            sortedArcs,
                            placeToIndex,
                            hypotheticCombination
                        )

                    if (combinationIsGood) {
                        acc.add(
                            Pair(
                                independentMultiConditionGroup,
                                indicesToTokenLists(hypotheticCombination, placeToIndex, filteredTokenSlice)
                            )
                        )
                        return@fold acc
                    }
                }
                acc.add(null)
                acc
            }

        if (foundCombinations.contains(null)) return null

        return foundCombinations.filterNotNull()
            .fold(mutableMapOf()) { acc: MutableMap<PlaceWrapper, List<TokenWrapper>>,
                                    (group, placeToTokens) ->
                for ((place, token) in placeToTokens) {
                    require(place !in acc) {
                        "at this stage there should be totally no intersections between places of solutions"
                    }
                    acc[place] = token
                }
                acc
            }
    }


    private fun preCheckHeuristicFiltering(tokenSlice: TokenSlice): TokenSlice? {
        // need to consider the conditions
        // filtering it is

        val preliminaryDumbCheck = transition.inputArcs.all { arc ->
            arc.consumptionSpec.complies(tokenSlice.amountAt(arc.fromPlace))
        }

        if (!preliminaryDumbCheck) {
            return null
        }

        // visited all required sync transitions
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
    ): Iterator<IndependentConditionsSolution> = iterator {
        val sortedArcs = independentMultiConditionGroup.conditionArcSortedByStrongestDescending()

        val (placeToIndex, combinationsIterator) = independentMultiConditionGroup.makeRandomCombinationIterator(
            filteredTokenSlice,
            shuffler = shuffler
        )

        while (combinationsIterator.hasNext()) {
            val hypotheticCombination = combinationsIterator.next()

            val combinationIsGood =
                checkCombination(
                    filteredTokenSlice,
                    independentMultiConditionGroup,
                    sortedArcs,
                    placeToIndex,
                    hypotheticCombination
                )

            if (combinationIsGood) {
                yield(
                    Pair(
                        independentMultiConditionGroup,
                        indicesToTokenLists(hypotheticCombination, placeToIndex, filteredTokenSlice)
                    )
                )
            }
        }
    }

    private fun createGroupSolutionCombinator(
        filteredTokenSlice: TokenSlice,
        shuffler: Shuffler,
        transition: TransitionWrapper,
    ): Iterable<List<IndependentConditionsSolution>> {
        val independentMultiConditionGroups = transition.independentMultiArcConditions

        val solutionSearchIterables = independentMultiConditionGroups.map {
            object : Iterable<IndependentConditionsSolution> {
                override fun iterator(): Iterator<IndependentConditionsSolution> {
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

    private fun checkCombination(
        tokenSlice: TokenSlice,
        independentMultiConditionGroup: IndependentMultiConditionGroup,
        sortedArcWrappers: List<InputArcWrapper>,
        placeToIndex: Map<PlaceWrapper, Int>,
        indicesCombination: List<List<Int>>
    ): Boolean {
        require(sortedArcWrappers.size == indicesCombination.size)

        val sharedEntriesPerPlace: Map<PlaceWrapper, Pair<List<TokenWrapper>, HashSet<Long>>> = step(
            """
                For each place respectively:
                  - find transition entries that are shared between all tokens at the place.
            """
        ) {
            sortedArcWrappers.map { inputArc ->
                val tokenIndices = indicesCombination[placeToIndex[inputArc.fromPlace]!!]
                val selectedTokens = tokenSlice.tokensAt(inputArc.fromPlace).selectTokens(tokenIndices)
                Triple(
                    inputArc.fromPlace,
                    selectedTokens,
                    selectedTokens.sharedTransitionEntries(inputArc.underConditions.map { it.syncTarget })
                )
            }.associateBy({ it.first }, { Pair(it.second, it.third) })
        }

        if (sharedEntriesPerPlace.any { it.value.second.isEmpty() }) {
            return false
        }

        val allMultiArcConditionsSatisfied = step(
            """
                Now we know that there are enough tokens at each place
                that satisfy condition only of the respective arc from one place.
                
                We need to verify that all conditions in $independentMultiConditionGroup
                are satisfied with this token combination.
            """
        ) {
            // cross-place by condition check,
            // for each group of intersecting conditions
            independentMultiConditionGroup.conditions.all { condition ->
                checkConditionIsSatisfied(
                    condition,
                    sharedEntriesPerPlace,
                )
            }
        }

        return allMultiArcConditionsSatisfied
    }

    private fun checkConditionIsSatisfied(
        multiArcCondition: MultiArcCondition,
        placesToTokensAndCommonEntires: Map<PlaceWrapper, Pair<List<TokenWrapper>, HashSet<Long>>>,
    ): Boolean {
        // need to confirm hashes that tokens at fromPlaces are not empty when intersected
        val common = hashSetOf<Long>()
        val placeIterator = multiArcCondition.fromPlaces.iterator()
        val syncTransition = listOf(multiArcCondition.syncTarget)

        if (!placeIterator.hasNext()) return false

        val firstPlace = placeIterator.next()

        placesToTokensAndCommonEntires[firstPlace]!!.let { (_, entriesHash) ->
            common.addAll(entriesHash)
            common.filterContainedInAny(syncTransition)
        }

        placeIterator.forEach { place ->
            val (_, entriesHash) = placesToTokensAndCommonEntires[place]!!
            common.retainAll(entriesHash)
            common.filterContainedInAny(syncTransition)

            if (common.isEmpty()) {
                return false
            }
        }
        return true
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