package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import net.bytebuddy.description.ByteCodeElement.Token
import ru.misterpotz.ocgena.simulation_v2.entities.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IndependentMultiConditionGroup
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenGenerator
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.resolveVariables
import java.util.*
import kotlin.collections.HashSet

sealed interface FullSolution {
    data class Tokens(
        val solutionTokens: SolutionTokens,
        val generatedTokens: List<TokenWrapper>
    ) : FullSolution

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
        val tokensCanAdditionallyGenerate: Int,
    ) : PartialSolutionCandidate

}

class TransitionSynchronizationArcSolver(
    val transition: TransitionWrapper,
) {

    fun getSolutionFinderIterable(
        tokenSlice: TokenSlice,
        shuffler: Shuffler,
        tokenGenerator: TokenGenerator
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
                listOfNotNull(
                    heavySynchronizationSearchCombinator.takeIf { transition.model.isSynchronizedMode() },
                    easyArcsSolutionCombinator
                )
            )

        return IteratorMapper(
            fullCombinator
        ) { independentSolutionCombination ->
            println(independentSolutionCombination)
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
        tokenSlice: TokenSlice,
        shuffler: Shuffler
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

            val combination = CombinationIterable(
                shuffling,
                combinationSize = tokensToTake
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

        val (placeToIndex, _, combinationsIterator) = independentMultiConditionGroup.makeRandomCombinationIterator(
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
                val goodCombination = indicesToTokenLists(
                    hypotheticCombination,
                    placeToIndex,
                    filteredTokenSlice
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

    private fun generateRequiredTokens(
        potentiallyUncompleteSolution: SortedMap<PlaceWrapper, List<TokenWrapper>>,
        tokensCanGenerate: SortedMap<PlaceWrapper, Int>,
    ): Map<PlaceWrapper, List<TokenWrapper>> {
        val requiredItems = mutableMapOf<PlaceWrapper, List<TokenWrapper>>(

        )
        for (place in potentiallyUncompleteSolution.keys) {
            val inputArc = transition.findInputArcByPlace(place)
            val tokenRequirement = inputArc.consumptionSpec.tokenRequirement()

            if (potentiallyUncompleteSolution[place]!!.size < tokenRequirement) {
                potentiallyUncompleteSolution[inputArc.fromPlace] = mutableListOf<TokenWrapper>().apply {
                    val tokensAlreadyExist = potentiallyUncompleteSolution[inputArc.fromPlace]!!.size
                    val generationAmount = tokenRequirement - tokensAlreadyExist
                    require(generationAmount <= tokensCanGenerate[inputArc.fromPlace]!!) {
                        "cannot generate for requirements, seems the upstream algorithm is broken"
                    }
                    val newgeneratedTokens = (0..<(generationAmount)).map {
                        tokenGenerator.generateRealToken(inputArc.fromPlace.objectType)
                    }
                    generatedTokens.addAll(newgeneratedTokens)

                    tokensCanGenerate[inputArc.fromPlace] =
                        tokensCanGenerate[inputArc.fromPlace]!! - newgeneratedTokens.size

                    addAll(potentiallyUncompleteSolution[inputArc.fromPlace] ?: emptyList())
                    addAll(newgeneratedTokens)
                }
            }
        }


        for (inputArc in transition.inputArcs) {
            val inputArc = transition.findInputArcByPlace(place)
            val tokenRequirement = inputArc.consumptionSpec.tokenRequirement()

            if (potentiallyUncompleteSolution[place]!!.size < tokenRequirement) {
                potentiallyUncompleteSolution[inputArc.fromPlace] = mutableListOf<TokenWrapper>().apply {
                    val tokensAlreadyExist = potentiallyUncompleteSolution[inputArc.fromPlace]!!.size
                    val generationAmount = tokenRequirement - tokensAlreadyExist
                    require(generationAmount <= tokensCanGenerate[inputArc.fromPlace]!!) {
                        "cannot generate for requirements, seems the upstream algorithm is broken"
                    }
                    val newgeneratedTokens = (0..<(generationAmount)).map {
                        tokenGenerator.generateRealToken(inputArc.fromPlace.objectType)
                    }
                    generatedTokens.addAll(newgeneratedTokens)

                    tokensCanGenerate[inputArc.fromPlace] =
                        tokensCanGenerate[inputArc.fromPlace]!! - newgeneratedTokens.size

                    addAll(potentiallyUncompleteSolution[inputArc.fromPlace] ?: emptyList())
                    addAll(newgeneratedTokens)
                }
            }
        }
    }

    private fun makeCompleteTokenSolution(
        potentiallyUncompleteSolution: SortedMap<PlaceWrapper, List<TokenWrapper>>,
        tokensCanGenerate: SortedMap<PlaceWrapper, Int>,
        conditionRequiredEntries: Map<IndependentMultiConditionGroup, Map<MultiArcCondition, HashSet<Long>>>,
        filteredTokenSlice: TokenSlice,
        shuffler: Shuffler,
        tokenGenerator: TokenGenerator
    ): FullSolution.Tokens {
        val placeToApplicableNewTokens = mutableMapOf<PlaceWrapper, MutableList<TokenWrapper>>()
        val generatedTokens = mutableListOf<TokenWrapper>()

        // 1. generate what not generated yet to complete requirements
        // 2. find candidates for further analysis - for all arcs, they must each be unconstrained or dependent on variables.
        // 3. for variable arcs do smart filling based on candidates from step 2
        // 4.

        for (place in potentiallyUncompleteSolution.keys) {
            val inputArc = transition.findInputArcByPlace(place)
            val tokenRequirement = inputArc.consumptionSpec.tokenRequirement()

            if (potentiallyUncompleteSolution[place]!!.size < tokenRequirement) {
                potentiallyUncompleteSolution[inputArc.fromPlace] = mutableListOf<TokenWrapper>().apply {
                    val tokensAlreadyExist = potentiallyUncompleteSolution[inputArc.fromPlace]!!.size
                    val generationAmount = tokenRequirement - tokensAlreadyExist
                    require(generationAmount <= tokensCanGenerate[inputArc.fromPlace]!!) {
                        "cannot generate for requirements, seems the upstream algorithm is broken"
                    }
                    val newgeneratedTokens = (0..<(generationAmount)).map {
                        tokenGenerator.generateRealToken(inputArc.fromPlace.objectType)
                    }
                    generatedTokens.addAll(newgeneratedTokens)

                    tokensCanGenerate[inputArc.fromPlace] =
                        tokensCanGenerate[inputArc.fromPlace]!! - newgeneratedTokens.size

                    addAll(potentiallyUncompleteSolution[inputArc.fromPlace] ?: emptyList())
                    addAll(newgeneratedTokens)
                }
            }
        }

        val resolvedVariablesSpace = potentiallyUncompleteSolution.resolveVariables(transition.inputArcs)
        val bufferVariablesSpace = resolvedVariablesSpace.copy()

        // найти для каждой арки потенциальный максимальный набор подходящих токенов
        for (inputArc in transition.inputArcs) {
            val fromPlace = inputArc.fromPlace

            if (inputArc.underConditions.isEmpty()) {
                when (inputArc.consumptionSpec) {
                    InputArcWrapper.ConsumptionSpec.AtLeastOne -> {
                        placeToApplicableNewTokens.getOrPut(fromPlace) { mutableListOf() }
                            .addAll(
                                // add all not already added
                                filteredTokenSlice.tokensAt(inputArc.fromPlace).filter {
                                    it !in potentiallyUncompleteSolution[inputArc.fromPlace]!!
                                }
                            )
                    }

                    is InputArcWrapper.ConsumptionSpec.Exact,
                    is InputArcWrapper.ConsumptionSpec.DependsOnVariable,
                    is InputArcWrapper.ConsumptionSpec.Variable -> Unit
                }

                val tokensCanGenerate = when (inputArc.consumptionSpec) {
                    InputArcWrapper.ConsumptionSpec.AtLeastOne -> {
                        tokensCanGenerate[inputArc.fromPlace] ?: 0
                    }

                    is InputArcWrapper.ConsumptionSpec.Exact -> {
                        (tokensCanGenerate[inputArc.fromPlace]
                            ?: 0).coerceAtMost(inputArc.consumptionSpec.tokenRequirement())
                    }
                    // separate token generation logics for lomazova arcs
                    is InputArcWrapper.ConsumptionSpec.DependsOnVariable,
                    is InputArcWrapper.ConsumptionSpec.Variable -> 0
                }
                if (tokensCanGenerate > 0) {
                    // can generate new tokens here
                    val generated = (0..<tokensCanGenerate).map {
                        tokenGenerator.generateRealToken(inputArc.fromPlace.objectType)
                    }
                    generatedTokens.addAll(generated)
                    placeToApplicableNewTokens[fromPlace] = mutableListOf<TokenWrapper>().apply {
                        addAll(placeToApplicableNewTokens[fromPlace] ?: emptyList())
                        addAll(generated)
                    }
                }
            }

            if (inputArc.consumptionSpec.isUnconstrained() && inputArc.underConditions.isNotEmpty()) {
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

            if (
                (
                        placeToApplicableNewTokens[fromPlace] == null ||
                                placeToApplicableNewTokens[fromPlace]!!.isEmpty()
                        ) &&
                (
                        tokensCanGenerate[fromPlace] == null || tokensCanGenerate[fromPlace] == 0
                        )
            ) {
                // leaving all new tokens per var arc as its
                continue
            }
            val variableName = varArc.consumptionSpec.castVar().variableName
            val newTokensGoodForVarArc = placeToApplicableNewTokens[fromPlace] ?: emptyList()
            val totalPotentialNewTokensForVarArc = newTokensGoodForVarArc.size + (tokensCanGenerate[fromPlace] ?: 0)

            var newTokensAmountForVarArcSolution = 0

            for (index in 0..<totalPotentialNewTokensForVarArc) {
                if (dependent.isEmpty()) {
                    newTokensAmountForVarArcSolution = tokensCanGenerate[varArc.fromPlace] ?: 0;
                    break;
                }
                bufferVariablesSpace.copyFrom(resolvedVariablesSpace)
                val initialVariableValue = bufferVariablesSpace.getVariable(variableName)
                bufferVariablesSpace.setVariable(variableName, initialVariableValue + index + 1)

                val allDependendsArcsSatisfied = dependent.all { dependentArc ->
                    dependentArc.totallySatisfiedWithTokens(
                        placeToApplicableNewTokens[dependentArc.fromPlace],
                        bufferVariablesSpace
                    )
                }
                if (allDependendsArcsSatisfied) {
                    newTokensAmountForVarArcSolution = index + 1
                    continue
                }
                val minAdditionalGenerationAmounts: MutableMap<InputArcWrapper, Int> = mutableMapOf()
                // uncomplied dependent arcs
                for (dependentArc in dependent) {
                    val tokens = placeToApplicableNewTokens[dependentArc.fromPlace]
                    val maxCanGenerate = (tokensCanGenerate[dependentArc.fromPlace] ?: 0)
                    var minSatisfactoryAmount: Int? = null
                    for (testTokenAmount in maxCanGenerate downTo 1) {
                        if (dependentArc.canBeSatisfiedWithAdditionalAmount(
                                tokens,
                                testTokenAmount,
                                bufferVariablesSpace
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
                if (minAdditionalGenerationAmounts.size == dependent.size) {
                    // reduce the amount of tokens that can be generated for dependent arcs and generate
                    for (dependentArc in dependent) {
                        val satisfactoryAmount = minAdditionalGenerationAmounts[dependentArc]!!
                        tokensCanGenerate[dependentArc.fromPlace] =
                            (tokensCanGenerate[dependentArc.fromPlace]!! - satisfactoryAmount).also {
                                require(it >= 0) { "consistency check failed" }
                            }

                        placeToApplicableNewTokens[dependentArc.fromPlace] =
                            mutableListOf<TokenWrapper>().apply {
                                addAll(placeToApplicableNewTokens[dependentArc.fromPlace] ?: emptyList())
                                addAll(
                                    (0..<satisfactoryAmount).map {
                                        val generated =
                                            tokenGenerator.generateRealToken(dependentArc.fromPlace.objectType)
                                        generatedTokens.add(generated)
                                        generated
                                    }
                                )
                            }
                    }
                } else {
                    // even with slight increase of the var arc there are no additional tokens that satisfy dependent amount
                    break;
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
                // no need to shuffle, as need to generate additional
                if (newTokensAmountForVarArcSolution > newTokensGoodForVarArc.size) {
                    tokensCanGenerate[fromPlace] = (tokensCanGenerate[fromPlace]!! -
                            (newTokensAmountForVarArcSolution - newTokensGoodForVarArc.size)).also {
                        require(it >= 0) { "consistency check" }
                    }
                    mutableListOf<TokenWrapper>().apply {
                        addAll(placeToApplicableNewTokens[fromPlace] ?: emptyList())
                        val tokensToAdditionallyGenerate =
                            newTokensAmountForVarArcSolution - newTokensGoodForVarArc.size
                        addAll((0..<(tokensToAdditionallyGenerate)).map {
                            val generated =
                                tokenGenerator.generateRealToken(varArc.fromPlace.objectType)
                            generatedTokens.add(generated)
                            generated
                        })
                    }
                } else {
                    placeToApplicableNewTokens[fromPlace]!!.selectTokens(
                        shuffler.makeShuffled(newTokensGoodForVarArc.indices)
                            .take(newTokensAmountForVarArcSolution)
                    )
                        .toMutableList()
                }


            placeToApplicableNewTokens[fromPlace] = newTokensToAddToVarArcSolution

            for (dependentArc in dependent) {
                val dependentArcPlace = dependentArc.fromPlace

                val tokensAtDependentArcPlace = placeToApplicableNewTokens[dependentArcPlace] ?: emptyList()
                // for each dependent place must
                val tokensToTake = dependentArc.consumptionSpec.tokensShouldTake(
                    tokensAtDependentArcPlace.size,
                    resolvedVariablesSpace
                )

                val newTokensToAddToDependentArcSolution = if (tokensAtDependentArcPlace.size == tokensToTake) {
                    placeToApplicableNewTokens[dependentArcPlace]!!
                } else {
                    mutableListOf<TokenWrapper>().apply {
                        addAll(
                            placeToApplicableNewTokens[dependentArcPlace]!!.selectTokens(
                                shuffler.makeShuffled(tokensAtDependentArcPlace.indices)
                                    .take(tokensToTake)
                            )
                        )
                    }
                }

                placeToApplicableNewTokens[dependentArcPlace] = newTokensToAddToDependentArcSolution
            }
        }

        if (placeToApplicableNewTokens.isEmpty()) return FullSolution.Tokens(potentiallyUncompleteSolution, emptyList())

        val completeSolution = buildMap {
            potentiallyUncompleteSolution.map { (place, tokens) ->
                put(place, buildList {
                    addAll(tokens)
                    addAll(placeToApplicableNewTokens[place] ?: emptyList())
                })
            }
        }
        return FullSolution.Tokens(completeSolution, generatedTokens)
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
