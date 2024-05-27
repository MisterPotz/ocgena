package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IndependentMultiConditionGroup
import ru.misterpotz.ocgena.simulation_v2.entities_storage.*
import java.util.*
import kotlin.collections.HashSet

sealed interface FullSolution {
    data class Tokens(
        val solutionTokens: SolutionTokens,
        val generatedTokens: List<TokenWrapper>
    ) : FullSolution

    data class Amounts(val solutionAmounts: SolutionAmounts) : FullSolution

    data class DoesNotExistSynchronized(val tokenSet: Set<TokenWrapper>): FullSolution

    fun toTokenSlice(): TokenSlice {
        return when (this) {
            is Amounts -> {
                SimpleTokenSlice.ofAmounts(solutionAmounts)
            }

            is Tokens -> {
                SimpleTokenSlice.of(solutionTokens)
            }

            is DoesNotExistSynchronized -> throw IllegalStateException()
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
        val tokensCanAdditionallyGenerate: Int,
    ) : PartialSolutionCandidate

}

class TransitionSynchronizationArcSolver(
    val transition: TransitionWrapper,
) {

    fun getSolutionFinderIterable(
        tokenSlice: TokenSlice,
        shuffler: Shuffler,
        tokenGenerator: TokenGenerator,
    ): Iterable<FullSolution>? {
        val filteredTokenSlice = preCheckSynchronizationHeuristicFiltering(tokenSlice) ?: return null

        val heavySynchronizationSearchCombinator = createGroupSolutionCombinator(
            filteredTokenSlice, shuffler, transition
        )
        val easyArcsSolutionCombinator = createUnconditionalArcsCombinationSolutionSuggester(tokenSlice, shuffler)

        val fullCombinator = IteratorsCombinator(
            listOfNotNull(
                heavySynchronizationSearchCombinator.takeIf { transition.model.isSynchronizedMode() },
                easyArcsSolutionCombinator
            )
        )
        var counter = 0
        return IteratorMapper(
            fullCombinator
        ) { independentSolutionCombination ->
//            if (counter % 100 == 0) {
//                println("combinating over $counter combination")
//            }
            completeSolutionFromPartials(
                independentSolutionCombination.flatten(),
                filteredTokenSlice,
                shuffler,
                tokenGenerator = tokenGenerator
            )
        }
    }

    private fun completeSolutionFromPartials(
        partials: List<PartialSolutionCandidate>,
        tokenSlice: TokenSlice,
        shuffler: Shuffler,
        tokenGenerator: TokenGenerator
    ): FullSolution {
        val fullSolutionMap = sortedMapOf<PlaceWrapper, List<TokenWrapper>>()
        val tokensCanGenerate = sortedMapOf<PlaceWrapper, Int>()
        val independentGroupSolutionTransitionEntries =
            mutableMapOf<IndependentMultiConditionGroup, Map<MultiArcCondition, HashSet<Long>>>()

        for (partial in partials) {
            when (partial) {
                is PartialSolutionCandidate.MultiConditionGroup -> {
                    require(
                        fullSolutionMap.keys.intersect(partial.potentiallyUncompleteCombination.keys).isEmpty()
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
                    tokensCanGenerate[partial.place] = partial.tokensCanAdditionallyGenerate
                }
            }
        }

        val completeSolution = makeCompleteTokenSolution(
            fullSolutionMap,
            tokensCanGenerate,
            independentGroupSolutionTransitionEntries,
            tokenSlice,
            shuffler = shuffler,
            tokenGenerator = tokenGenerator
        )
        return completeSolution
    }

    private fun createUnconditionalArcsCombinationSolutionSuggester(
        tokenSlice: TokenSlice, shuffler: Shuffler
    ): Iterable<List<PartialSolutionCandidate.Unconditional>> {
        val allUnconditionalCombinationsIterator = transition.unconditionalInputArcs.map { unconditionalArc ->
            val realTokenAmount = unconditionalArc.fromPlace.tokenAmountAt(tokenSlice)
            val tokens = tokenSlice.tokensAt(unconditionalArc.fromPlace)

            val tokensCanGenerate = realTokenAmount - tokenSlice.tokensAt(unconditionalArc.fromPlace).size

            val tokensToTake = if (unconditionalArc.isAalstArc()) {
                // do not select, take all for aalst arc, as its not conditioned, all tokens can be consumed
                tokens.size
            } else {
                unconditionalArc.consumptionSpec.tokenRequirement()
            }

            val shuffling = if (unconditionalArc.isAalstArc()) {
                // do not shuffle
                tokens.indices.toList()
            } else {
                unconditionalArc.fromPlace.getTokens(tokenSlice).size.takeIf { it > 0 }?.let {
                    shuffler.makeShuffled(0..<it)
                } ?: listOf(0)
            }

            val combination = CombinationIterableInt(
                shuffling, combinationSize = tokensToTake
            )

            IteratorMapper(combination) { combination ->
                PartialSolutionCandidate.Unconditional(
                    unconditionalArc.fromPlace,
                    tokenSlice.tokensAt(unconditionalArc.fromPlace).selectTokens(combination),
                    tokensCanAdditionallyGenerate = tokensCanGenerate
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

        if (!preliminaryDumbCheck) {
            return null
        }

        if (!transition.model.isSynchronizedMode()) {
            return tokenSlice
        }


        val filteredTokenSlice = tokenSlice.filterTokensInPlaces(transition.prePlaces) { token, place ->
            val inputArc = transition.inputArcBy(place.placeId)
            if (inputArc.syncTransitions.isNotEmpty()) {
                token.visitedTransitions.containsAll(inputArc.syncTransitions)
            } else {
                true
            }
        }

        val secondPreliminaryDumbCheck = transition.inputArcsSortedByRequiredTokens.all { arc ->
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

        val (placeToIndex, _, combinationsIterator) = independentMultiConditionGroup.makeRandomCombinationIterator(
            filteredTokenSlice, shuffler = shuffler
        )

        while (combinationsIterator.hasNext()) {
            val hypotheticCombination = combinationsIterator.next()

            val perPlaceSharedEntries = makePerPlaceSharedTransitionEntries(
                sortedArcs, filteredTokenSlice, placeToIndex, indicesCombination = hypotheticCombination
            )
            val conditionSatisfactoryEntries = getConditionSatisfactoryEntries(
                independentMultiConditionGroup, sortedArcs, hypotheticCombination, perPlaceSharedEntries
            )

            if (conditionSatisfactoryEntries.isNotEmpty()) {
                val goodCombination = indicesToTokenLists(
                    hypotheticCombination, placeToIndex, filteredTokenSlice
                )
                yield(
                    PartialSolutionCandidate.MultiConditionGroup(
                        independentMultiConditionGroup,
                        conditionSatisfactoryEntries,
                        potentiallyUncompleteCombination = goodCombination,
                    )
                )
            }
        }
    }

    data class TokensWithGenerated(
        val solution: Map<PlaceWrapper, List<TokenWrapper>>,
    )

    private fun expandAalstArcs(
        currentSolution: Map<PlaceWrapper, List<TokenWrapper>>,
        applicableTokenStorage: ApplicableTokenStorage,
        tokenGenerator: TokenGenerator
    ): TokensWithGenerated {
        val newSolution = mutableMapOf<PlaceWrapper, List<TokenWrapper>>().apply {
            putAll(currentSolution)
        }

        // найти для каждой арки потенциальный максимальный набор подходящих токенов
        for (inputArc in transition.inputArcs) {
            val fromPlace = inputArc.fromPlace

            if (inputArc.isAalstArc()) {
                val currentArcTokens = currentSolution[fromPlace]!!
                val applicableTokens = applicableTokenStorage.remainingTokens(fromPlace)

                // add all not already added
                val toAppend = applicableTokens
                val tokensCanGenerateAdditionally = applicableTokenStorage.remainingGenerations(inputArc.fromPlace)

                val newGenerated = if (tokensCanGenerateAdditionally != 0) {
                    applicableTokenStorage.updateGenerations(fromPlace) { 0 }
                    // can generate new tokens here
                    val newGenerated = (0..<tokensCanGenerateAdditionally).map {
                        tokenGenerator.generateRealToken(inputArc.fromPlace.objectType)
                    }
                    applicableTokenStorage.recordGenerated(newGenerated)
                    newGenerated
                } else emptyList()
                newSolution[fromPlace] = mutableListOf<TokenWrapper>().apply {
                    addAll(currentArcTokens)
                    addAll(toAppend)
                    addAll(newGenerated)
                }
                applicableTokenStorage.remainingTokens(inputArc.fromPlace).removeAll(newSolution[fromPlace]!!)
            }
        }

        return TokensWithGenerated(newSolution)
    }

    private fun recordSliceRemainingApplicableTokens(
        currentSolution: Map<PlaceWrapper, List<TokenWrapper>>,
        conditionRequiredEntries: Map<IndependentMultiConditionGroup, Map<MultiArcCondition, HashSet<Long>>>,
        applicableTokenStorage: ApplicableTokenStorage,
        tokenSlice: TokenSlice
    ) {
        val applicableTokens = mutableMapOf<PlaceWrapper, MutableList<TokenWrapper>>().apply {
            for (key in currentSolution.keys) {
                put(key, mutableListOf())
            }
        }

        for (arc in transition.inputArcs) {
            if (arc.consumptionSpec.isVariable()) {
                val fromPlace = arc.fromPlace
                val allPlaceTokens = tokenSlice.tokensAt(fromPlace)
                val alreadySolutionTokens = currentSolution[fromPlace]!!

                if (arc.underConditions.isEmpty()) {
                    applicableTokens[fromPlace] = allPlaceTokens.filter { it !in alreadySolutionTokens }.toMutableList()
                } else {
                    // if arc under condition it is not allowed to have generations
                    applicableTokenStorage.updateGenerations(fromPlace) { 0 }
                    val conditionGroupEntries = conditionRequiredEntries[arc.independentGroup]!!

                    for (arcCondition in arc.underConditions) {
                        val requiredConditionEntries = conditionGroupEntries[arcCondition]!!

                        applicableTokens.getOrPut(fromPlace) { mutableListOf() }.addAll(allPlaceTokens.filter { token ->
                            token !in alreadySolutionTokens // && token.participatedInAll(requiredConditionEntries)
                        })
                    }
                }

            }
        }
        for ((place, applicable) in applicableTokens) {
            applicableTokenStorage.remainingTokens(place).addAll(applicable)
        }
    }

    private fun fillExactArcsMissingTokens(
        currentSolution: SortedMap<PlaceWrapper, List<TokenWrapper>>,
        applicableTokenStorage: ApplicableTokenStorage,
        tokenGenerator: TokenGenerator
    ): TokensWithGenerated {
        val newSolution = mutableMapOf<PlaceWrapper, List<TokenWrapper>>().apply {
            putAll(currentSolution)
        }

        for (place in currentSolution.keys) {
            val inputArc = transition.findInputArcByPlace(place)
            val tokenRequirement = inputArc.consumptionSpec.tokenRequirement()

            if (inputArc.isExactArc() && currentSolution[place]!!.size < tokenRequirement) {
                newSolution[inputArc.fromPlace] = mutableListOf<TokenWrapper>().apply {
                    val tokensAlreadyExist = currentSolution[inputArc.fromPlace]!!.size
                    val generationAmount = tokenRequirement - tokensAlreadyExist
                    require(generationAmount <= applicableTokenStorage.remainingGenerations(inputArc.fromPlace)) {
                        "cannot generate for requirements, seems the upstream algorithm is broken"
                    }
                    val newgeneratedTokens = (0..<(generationAmount)).map {
                        tokenGenerator.generateRealToken(inputArc.fromPlace.objectType)
                    }
                    applicableTokenStorage.recordGenerated(newgeneratedTokens)
                    applicableTokenStorage.updateGenerations(inputArc.fromPlace) {
                        0
                    }

                    addAll(currentSolution[inputArc.fromPlace] ?: emptyList())
                    addAll(newgeneratedTokens)
                }
            }
        }
        return TokensWithGenerated(newSolution)
    }

    class ApplicableTokenStorage(
        tokens: Map<PlaceWrapper, List<TokenWrapper>> = mutableMapOf(),
        canGenerate: Map<PlaceWrapper, Int> = mutableMapOf(),
    ) {
        private val generated = mutableListOf<TokenWrapper>()
        private val tokens = mutableMapOf<PlaceWrapper, MutableList<TokenWrapper>>().apply {
            putAll(tokens.mapValues { it.value.toMutableList() })
        }
        private val canGenerate: MutableMap<PlaceWrapper, Int> = mutableMapOf<PlaceWrapper, Int>().apply {
            putAll(canGenerate)
        }

        fun remainingTokens(place: PlaceWrapper): MutableList<TokenWrapper> = tokens.getOrPut(place) { mutableListOf() }
        fun remainingGenerations(place: PlaceWrapper): Int = canGenerate.getOrPut(place) { 0 }

        fun updateGenerations(place: PlaceWrapper, block: (Int) -> Int) {
            canGenerate[place] = block(remainingGenerations(place)).also {
                require(it >= 0)
            }
        }

        fun totalTokensAvailable(place: PlaceWrapper): Int {
            return remainingTokens(place).size + remainingGenerations(place)
        }

        fun recordGenerated(tokens: Collection<TokenWrapper>) {
            generated.addAll(tokens)
        }

        fun getGenerated(): List<TokenWrapper> {
            return generated
        }
    }

    private fun selectAndGenerateForLomazovaArcFromRemaining(
        amount: Int,
        arc: InputArcWrapper,
        applicableTokenStorage: ApplicableTokenStorage,
        shuffler: Shuffler,
        tokenGenerator: TokenGenerator,
    ): List<TokenWrapper> {
        val fromPlace = arc.fromPlace
        val availableThroughTokens = applicableTokenStorage.remainingTokens(fromPlace)

        val selectedFromRemaining =
            availableThroughTokens.selectTokens(shuffler.makeShuffled(availableThroughTokens.indices)).take(amount)
        applicableTokenStorage.remainingTokens(fromPlace).removeAll(selectedFromRemaining)

        val leftToMake =
            (amount - selectedFromRemaining.size).coerceAtMost(applicableTokenStorage.remainingGenerations(fromPlace))

        val generated = applicableTokenStorage.remainingGenerations(fromPlace).let {
            (0..<leftToMake).map {
                tokenGenerator.generateRealToken(fromPlace.objectType)
            }
        }
        applicableTokenStorage.updateGenerations(fromPlace) { it - generated.size }
        applicableTokenStorage.recordGenerated(generated)

        return buildList {
            addAll(selectedFromRemaining)
            addAll(generated)
        }
    }

    private fun expandLomazovaArcs(
        currentSolution: Map<PlaceWrapper, List<TokenWrapper>>,
        applicableTokenStorage: ApplicableTokenStorage,
        tokenGenerator: TokenGenerator,
        shuffler: Shuffler,
    ): TokensWithGenerated {
        val newSolution = mutableMapOf<PlaceWrapper, MutableList<TokenWrapper>>().apply {
            putAll(currentSolution.mapValues { it.value.toMutableList() })
        }
        val resolvedVariablesSpace = currentSolution.resolveVariables(transition.inputArcs)
        val bufferVariablesSpace = resolvedVariablesSpace.copy()

        // попытаться из найденных подходящих токенов выбрать под большее покрытие арок с Lomazova variable arcs
        for ((varArc, dependent) in transition.lomazovaVariableArcsToDependent) {
            // try to gather more tokens for var arc
            val fromPlace = varArc.fromPlace
            require(currentSolution[fromPlace] != null)

            val variableName = varArc.consumptionSpec.castVar().variableName
            val totalPotentialNewTokensForVarArc = applicableTokenStorage.totalTokensAvailable(fromPlace)
            var newTokensAmountForVarArcSolution = 0

            for (index in 0..<totalPotentialNewTokensForVarArc) {
                if (dependent.isEmpty()) {
                    newTokensAmountForVarArcSolution = totalPotentialNewTokensForVarArc
                    break;
                }
                bufferVariablesSpace.copyFrom(resolvedVariablesSpace)
                val initialVariableValue = bufferVariablesSpace.getVariable(variableName)
                bufferVariablesSpace.setVariable(variableName, initialVariableValue + index + 1)

                val allDependendsArcsSatisfied = dependent.all { dependentArc ->
                    val totalDependentTokens =
                        currentSolution[dependentArc.fromPlace]!!.size + applicableTokenStorage.totalTokensAvailable(
                            dependentArc.fromPlace
                        )

                    dependentArc.consumptionSpec.strongComplies(
                        totalDependentTokens, bufferVariablesSpace
                    )
                }
                if (allDependendsArcsSatisfied) {
                    newTokensAmountForVarArcSolution = index + 1
                } else {
                    break;
                }
            }

            // leaving current var arc solution for the variable as it is
            if (newTokensAmountForVarArcSolution == 0) continue

            // updating the variable space for this variable
            resolvedVariablesSpace.setVariable(
                variableName, resolvedVariablesSpace.getVariable(variableName) + newTokensAmountForVarArcSolution
            )
            val newTokensForVarArc = selectAndGenerateForLomazovaArcFromRemaining(
                newTokensAmountForVarArcSolution, varArc, applicableTokenStorage, shuffler, tokenGenerator
            )
            newSolution.getOrPut(fromPlace) { mutableListOf() }.apply {
                addAll(newTokensForVarArc)
            }

            val minAdditionalGenerationAmounts: MutableMap<InputArcWrapper, Int> = mutableMapOf()
            // uncomplied dependent arcs
            for (dependentArc in dependent) {
                val totalCanRequest =
                    currentSolution[dependentArc.fromPlace]!!.size + applicableTokenStorage.totalTokensAvailable(
                        dependentArc.fromPlace
                    )

                var minSatisfactoryAmount: Int? = null
                for (testTokenAmount in totalCanRequest downTo 1) {
                    if (dependentArc.consumptionSpec.strongComplies(
                            testTokenAmount, resolvedVariablesSpace
                        )
                    ) {
                        minSatisfactoryAmount = testTokenAmount
                    } else {
                        break
                    }
                }
                if (minSatisfactoryAmount != null) {
                    minAdditionalGenerationAmounts[dependentArc] = minSatisfactoryAmount
                }
            }
            // all input arcs have amount of tokens that can be generated for their satisfaction
            require(minAdditionalGenerationAmounts.size == dependent.size) {
                "weird case. Conditions were checked in a loop for determining the variable value. This case shouldn't have happened at all."
            }
            for (dependentArc in dependent) {
                val satisfactoryAdditionalAmount = minAdditionalGenerationAmounts[dependentArc]!!
                val toGenerateAmount = (satisfactoryAdditionalAmount - currentSolution[dependentArc.fromPlace]!!.size).coerceAtLeast(0)
                val newTokensForDependentArc = selectAndGenerateForLomazovaArcFromRemaining(
                    toGenerateAmount,
                    arc = dependentArc,
                    applicableTokenStorage,
                    shuffler,
                    tokenGenerator
                )
                newSolution.getOrPut(dependentArc.fromPlace) { mutableListOf() }.apply {
                    addAll(newTokensForDependentArc)
                }
            }
        }

        return TokensWithGenerated(newSolution)
    }

    private fun makeCompleteTokenSolution(
        potentiallyUncompleteSolution: SortedMap<PlaceWrapper, List<TokenWrapper>>,
        tokensCanGenerate: SortedMap<PlaceWrapper, Int>,
        conditionRequiredEntries: Map<IndependentMultiConditionGroup, Map<MultiArcCondition, HashSet<Long>>>,
        filteredTokenSlice: TokenSlice,
        shuffler: Shuffler,
        tokenGenerator: TokenGenerator
    ): FullSolution.Tokens {
        val applicableTokenStorage = ApplicableTokenStorage(mapOf(), canGenerate = tokensCanGenerate)

        // 1. generate what not generated yet to complete requirements
        val (minSolution) = fillExactArcsMissingTokens(
            potentiallyUncompleteSolution, applicableTokenStorage, tokenGenerator
        )
        // 2. all remaining tokens in slice
        recordSliceRemainingApplicableTokens(
            minSolution,
            conditionRequiredEntries,
            applicableTokenStorage,
            filteredTokenSlice,
        )

        // 3. find candidates for further analysis - for all arcs, they must each be unconstrained or dependent on variables.
        val (solutionStage2) = expandAalstArcs(
            minSolution, applicableTokenStorage, tokenGenerator
        )
        // 4. for variable arcs do smart filling based on candidates from step 2
        val (solutionStage3) = expandLomazovaArcs(
            solutionStage2,
            applicableTokenStorage,
            tokenGenerator,
            shuffler,
        )

        return FullSolution.Tokens(solutionStage3, applicableTokenStorage.getGenerated())
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
                        filteredTokenSlice, shuffler, it
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
                multiArcCondition = condition, placesToTokensAndCommonEntries = perPlaceSharedEntries
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
        val thisCollection = this
        if (isEmpty()) return emptyList()

        val list = MutableList(indices.size) { first() }

        forEachIndexed { index, indexedValue ->
            val place = indices.indexOf(index)
            if (place > -1) {
                list[place] = indexedValue
            }
        }
        return list
    }

    private fun List<TokenWrapper>.sharedTransitionEntries(includeEntriesOf: List<TransitionWrapper>): HashSet<Long> {
//        val sharedTransitionEntries = hashSetOf<Long>().apply {
//            addAll(get(0).allParticipatedTransitionEntries)
//        }
//
//        for (i in 1..<size) {
//            val tokenI = get(i)
//            if (sharedTransitionEntries.isEmpty()) {
//                return sharedTransitionEntries
//            }
//            sharedTransitionEntries.retailAllShared(tokenI)
//        }
//
//        sharedTransitionEntries.filterContainedInAny(includeEntriesOf)
        return hashSetOf()
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

//    private fun HashSet<Long>.retailAllShared(tokenWrapper: TokenWrapper) {
//        this.retainAll(tokenWrapper.allParticipatedTransitionEntries)
//    }

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

}
