package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IndependentMultiConditionGroup
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenGenerator
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.resolveVariables

class TransitionSyncV2FullSolutionFinder(
    private val transition: TransitionWrapper,
    private val shuffler: Shuffler,
    private val tokenGenerator: TokenGenerator
) {
    private val minimalSolutionFinder = TransitionSyncV2MinimalIndepdendentsSolutionFinder(transition, shuffler)

    fun asIterable(tokenSlice: TokenSlice, lastTokenSet: Set<TokenWrapper>? = null): Iterable<FullSolution> {
        return object : Iterable<FullSolution> {
            override fun iterator(): Iterator<FullSolution> {
                return findSolution(tokenSlice, lastTokenSet)
            }
        }
    }


    fun findSolutionForSynchronizedModeSingle(tokenSlice: TokenSlice): FullSolution.Tokens? {
        val filtered = minimalSolutionFinder.preCheckSynchronizationHeuristicFiltering(tokenSlice) ?: return null

        val presolution: Map<IndependentMultiConditionGroup, TransitionSyncV2MinimalIndepdendentsSolutionFinder.IndependentGroupSolution> =
            minimalSolutionFinder.presolutionSingle(filtered) ?: return null

        val currentSolution = SimpleTokenSlice.build { transition.prePlaces.forEach { addRelatedPlace(it) } }
        val minimalSolution = minimalSolutionFinder.usePresolutionForMinimal(presolution)
        currentSolution.plusInPlace(minimalSolution)

        val tokenRealizer = TokenRealizer(tokenGenerator)
        val arcToIterable by lazy(LazyThreadSafetyMode.NONE) {
            makeHeteregenousArcTokensIterator(
                transition,
                presolution,
                filtered,
                minimalSolution
            )
        }

        expandLomazovaArcs(arcToIterable, minimalSolution, tokenRealizer)?.let {
            currentSolution.plusInPlace(it)
        }
        expandAalstArcs(arcToIterable, minimalSolution, tokenRealizer)?.let {
            currentSolution.plusInPlace(it)
        }
        expandExactArcs(arcToIterable, minimalSolution, tokenRealizer)?.let {
            currentSolution.plusInPlace(it)
        }

        val fullSolution = FullSolution.Tokens(
            solutionTokens = buildMap {
                for (i in currentSolution.relatedPlaces) {
                    put(i, currentSolution.tokensAt(i))
                }
            },
            generatedTokens = tokenRealizer.generatedTokens
        )

        return fullSolution
    }

    fun findSolutionForSynchronizedMode(
        tokenSlice: TokenSlice,
        lastTokenSet: Set<TokenWrapper>?
    ): Iterator<FullSolution> = iterator {
        val filtered = minimalSolutionFinder.preCheckSynchronizationHeuristicFiltering(tokenSlice) ?: return@iterator
        val checkingSet = tokenSliceToSet(filtered)
        println("checking set ${checkingSet.size}")
        if (lastTokenSet != null &&
            lastTokenSet.containsAll(checkingSet) &&
            checkingSet.size <= lastTokenSet.size
        ) {
            yield(FullSolution.DoesNotExistSynchronized(checkingSet))
        }
        val synchronizationPresolutionIterator: Iterator<Map<IndependentMultiConditionGroup, TransitionSyncV2MinimalIndepdendentsSolutionFinder.IndependentGroupSolution>> =
            minimalSolutionFinder.presolutionIterator(filtered)

        var hadAtLeastOneSolution = false
        for (presolution in synchronizationPresolutionIterator) {
//            println(presolution)
            val currentSolution = SimpleTokenSlice.build { transition.prePlaces.forEach { addRelatedPlace(it) } }
            val minimalSolution = minimalSolutionFinder.usePresolutionForMinimal(presolution)
            currentSolution.plusInPlace(minimalSolution)

            val tokenRealizer = TokenRealizer(tokenGenerator)
            val arcToIterable by lazy(LazyThreadSafetyMode.NONE) {
                makeHeteregenousArcTokensIterator(
                    transition,
                    presolution,
                    filtered,
                    minimalSolution
                )
            }

            expandLomazovaArcs(arcToIterable, minimalSolution, tokenRealizer)?.let {
                currentSolution.plusInPlace(it)
            }
            expandAalstArcs(arcToIterable, minimalSolution, tokenRealizer)?.let {
                currentSolution.plusInPlace(it)
            }
            expandExactArcs(arcToIterable, minimalSolution, tokenRealizer)?.let {
                currentSolution.plusInPlace(it)
            }

            val fullSolution = FullSolution.Tokens(
                solutionTokens = buildMap {
                    for (i in currentSolution.relatedPlaces) {
                        put(i, currentSolution.tokensAt(i))
                    }
                },
                generatedTokens = tokenRealizer.generatedTokens
            )

            hadAtLeastOneSolution = true
            yield(fullSolution)
        }
        if (!hadAtLeastOneSolution) {
            yield(
                FullSolution.DoesNotExistSynchronized(
                    tokenSliceToSet(filtered)
                )
            )
        }
    }

    private fun tokenSliceToSet(tokenSlice: TokenSlice): Set<TokenWrapper> {
        return buildSet {
            for ((place, tokens) in tokenSlice.byPlaceIterator()) {
                addAll(tokens)
            }
        }
    }

    private fun findSolutionSimpleMode(tokenSlice: TokenSlice): Iterator<FullSolution.Tokens> = iterator {
        val filtered = minimalSolutionFinder.preCheckSynchronizationHeuristicFiltering(tokenSlice) ?: return@iterator

        val combinationIterable = IteratorsCombinator(
            transition.inputArcs.map { makePlaceTokensIteratorByArcRequirement(filtered, it, shuffler) }
        )

        val iterable = combinationIterable.takeIf { it.iterator().hasNext() } ?: 0..<1

        for (combination in iterable) {
            val currentSolution = SimpleTokenSlice.build { transition.prePlaces.forEach { addRelatedPlace(it) } }
            val tokenRealizer = TokenRealizer(tokenGenerator)

            val arcToIterable by lazy(LazyThreadSafetyMode.NONE) {
                makeHeteregenousArcTokensIterator(
                    transition,
                    mapOf(),
                    filtered,
                    currentSolution = null
                )
            }

            expandLomazovaArcs(arcToIterable, currentSolution, tokenRealizer)?.let {
                currentSolution.plusInPlace(it)
            }
            expandAalstArcs(arcToIterable, currentSolution, tokenRealizer)?.let {
                currentSolution.plusInPlace(it)
            }
            expandExactArcs(arcToIterable, currentSolution, tokenRealizer)?.let {
                currentSolution.plusInPlace(it)
            }

            val fullSolution = FullSolution.Tokens(
                solutionTokens = buildMap {
                    for (i in currentSolution.relatedPlaces) {
                        put(i, currentSolution.tokensAt(i))
                    }
                },
                generatedTokens = tokenRealizer.generatedTokens
            )

            yield(fullSolution)
        }
    }

    fun findSolutionSingle(tokenSlice: TokenSlice): Iterator<FullSolution.Tokens> {
        return if (transition.model.isSynchronizedMode() && transition.inputArcConditions.isNotEmpty()) {
            println("synchronized mode")
            listOfNotNull(findSolutionForSynchronizedModeSingle(tokenSlice)).iterator()
        } else {
            println("normal mode")
            findSolutionSimpleMode(tokenSlice)
        }
    }

    fun findSolution(tokenSlice: TokenSlice, lastTokenSet: Set<TokenWrapper>? = null): Iterator<FullSolution> {
        return if (transition.model.isSynchronizedMode() &&
            transition.inputArcConditions.isNotEmpty()
        ) {
            println("synchronized mode $this")
            findSolutionForSynchronizedMode(tokenSlice, lastTokenSet)
        } else {
            println("normal mode $this")
            findSolutionSimpleMode(tokenSlice)
        }
    }

    private fun expandExactArcs(
        arcToApplicableTokens: List<ArcToApplicableTokens>,
        currentSolution: TokenSlice,
        tokenRealizer: TokenRealizer,
    ): TokenSlice? {
        var added = false
        val consumedTokens by lazy(LazyThreadSafetyMode.NONE) {
            added = true
            mutableMapOf<InputArcWrapper, MutableList<Token>>()
        }

        // найти для каждой арки потенциальный максимальный набор подходящих токенов
        for (inputArc in transition.inputArcs) {
            if (inputArc.isExactArc()) {
                val tokenRequirement = inputArc.consumptionSpec.tokenRequirement()
                val current = currentSolution.tokensAt(inputArc.fromPlace).size
                val iterator = arcToApplicableTokens.forArc(inputArc)

                for (i in current..<tokenRequirement) {
                    val newToken = iterator.next()
                    consumedTokens.computeIfAbsent(inputArc) {
                        mutableListOf()
                    }.add(newToken)
                }
            }
        }

        return if (added) {
            SimpleTokenSlice.build {
                currentSolution.relatedPlaces.forEach { addRelatedPlace(it) }
                consumedTokens.forEach { (arc, tokens) ->
                    addTokens(arc.fromPlace, tokens.map { tokenRealizer.realizeIfNeed(it) })
                }
            }
        } else {
            null
        }
    }

    private fun expandAalstArcs(
        arcToApplicableTokens: List<ArcToApplicableTokens>,
        currentSolution: TokenSlice,
        tokenRealizer: TokenRealizer
    ): TokenSlice? {
        var added = false
        val consumedTokens by lazy(LazyThreadSafetyMode.NONE) {
            added = true
            mutableMapOf<InputArcWrapper, MutableList<Token>>()
        }

        // найти для каждой арки потенциальный максимальный набор подходящих токенов
        for (inputArc in transition.inputArcs) {
            if (inputArc.isAalstArc()) {
                val iterator = arcToApplicableTokens.forArc(inputArc)
                while (iterator.hasNext()) {
                    val newToken = iterator.next()
                    consumedTokens.computeIfAbsent(inputArc) {
                        mutableListOf()
                    }.add(newToken)
                }
            }
        }

        return if (added) {
            SimpleTokenSlice.build {
                currentSolution.relatedPlaces.forEach { addRelatedPlace(it) }
                consumedTokens.forEach { (arc, tokens) ->
                    addTokens(arc.fromPlace, tokens.map { tokenRealizer.realizeIfNeed(it) })
                }
            }
        } else {
            null
        }
    }

    private fun List<ArcToApplicableTokens>.forArc(inputArc: InputArcWrapper): Iterator<Token> {
        return find { it.inputArc == inputArc }!!.iterator
    }

    private fun expandSingleLomazovaVarArc(
        inputArc: InputArcWrapper,
        dependent: Collection<InputArcWrapper>,
        arcToApplicableTokens: List<ArcToApplicableTokens>,
        currentSolution: TokenSlice,
        tokenRealizer: TokenRealizer
    ): TokenSlice? {
        // var arc can be synchronized and unsynchronized
        // dependent arc can be synchronized and unsynchronized
        // need to test
        val resolvedVariablesSpace = currentSolution.resolveVariables(transition.inputArcs)
        val bufferVariablesSpace = resolvedVariablesSpace.copy()
        val varName = inputArc.consumptionSpec.castVar().variableName
        var added = false
        val consumedTokens by lazy(LazyThreadSafetyMode.NONE) {
            added = true
            mutableMapOf<InputArcWrapper, MutableList<Token>>()
        }
        // initial initialization pass
        if (resolvedVariablesSpace.getVariable(varName) == 0) {
            val token = arcToApplicableTokens.forArc(inputArc).next()
            resolvedVariablesSpace.setVariable(varName, 1)
            bufferVariablesSpace.copyFrom(resolvedVariablesSpace)
            consumedTokens.computeIfAbsent(inputArc) { mutableListOf() }.add(token)
        }
        for (depArc in dependent) {
            val tokenRequirement = depArc.consumptionSpec.tokenRequirement()
            val currentTokens = currentSolution.tokensAt(depArc.fromPlace).size

            if (!depArc.consumptionSpec.weakComplies(currentSolution.amountAt(depArc.fromPlace))) {
                val iterator = arcToApplicableTokens.forArc(depArc)
                for (i in currentTokens..<tokenRequirement) {
                    require(iterator.hasNext()) { " at this point there already must be enough tokens" }
                    val newToken = iterator.next()
                    consumedTokens.computeIfAbsent(depArc) { mutableListOf() }.add(newToken)
                }
            }
        }

        // trying to expand
        for (varToken in arcToApplicableTokens.forArc(inputArc)) {
            bufferVariablesSpace.setVariable(varName, bufferVariablesSpace.getVariable(varName) + 1)
            val hypothesisTokens by lazy(LazyThreadSafetyMode.NONE) {
                mutableMapOf<InputArcWrapper, MutableList<Token>>()
            }
            hypothesisTokens.computeIfAbsent(inputArc) { mutableListOf() }.add(varToken)
            var successfulHypothesis = true
            for (depArc in dependent) {
                // try consume the tokens for dependent arc until it is satisfied
                val iterator = arcToApplicableTokens.forArc(depArc)
                var foundPerDepCombination = false
                while (iterator.hasNext()) {
                    val token = iterator.next()

                    hypothesisTokens.computeIfAbsent(depArc) { mutableListOf() }.add(token)

                    if (
                        depArc.consumptionSpec.strongComplies(
                            currentSolution.amountAt(depArc.fromPlace) +
                                    consumedTokens.computeIfAbsent(depArc) { mutableListOf() }.size +
                                    hypothesisTokens.computeIfAbsent(depArc) { mutableListOf() }.size,
                            bufferVariablesSpace
                        )
                    ) {
                        foundPerDepCombination = true
                        break;
                    }
                }
                if (!foundPerDepCombination) {
                    successfulHypothesis = false
                    break;
                }
                // the increment of tokens of main var arc made dependents not comply with their conditions
            }
            if (successfulHypothesis) {
                hypothesisTokens.forEach { (arc, tokens) ->
                    consumedTokens.computeIfAbsent(arc) { mutableListOf() }.addAll(tokens)
                }
            }
        }
        if (!added) return null

        return SimpleTokenSlice.build {
            consumedTokens.forEach { (arc, tokens) ->
                val realizedTokens = tokens.map { tokenRealizer.realizeIfNeed(it) }
                addTokens(arc.fromPlace, realizedTokens)
            }
        }
    }

    private fun expandLomazovaArcs(
        arcToApplicableTokens: List<ArcToApplicableTokens>,
        currentSolution: TokenSlice,
        tokenRealizer: TokenRealizer
    ): TokenSlice? {
        var added = false
        val compoundTokenSlice by lazy(LazyThreadSafetyMode.NONE) {
            added = true
            SimpleTokenSlice.build { for (place in currentSolution.relatedPlaces) addRelatedPlace(place) }
        }

        for ((varArc, dependent) in transition.lomazovaVariableArcsToDependent) {
            val expandedArcTokens =
                expandSingleLomazovaVarArc(varArc, dependent, arcToApplicableTokens, currentSolution, tokenRealizer)

            if (expandedArcTokens != null) {
                compoundTokenSlice.plusInPlace(expandedArcTokens)
            }
        }

        return if (!added) {
            null
        } else {
            compoundTokenSlice
        }
    }


    private class TokenRealizer(val tokenGenerator: TokenGenerator) {
        val generatedTokens = mutableListOf<TokenWrapper>()
        fun realizeIfNeed(token: Token): TokenWrapper {
            return when (token) {
                is TokenWrapper -> token
                is UngeneratedToken -> token.generate(tokenGenerator).also { generatedTokens.add(it) }
            }
        }
    }
}

class TokenChecker(
    private val presolution: Map<IndependentMultiConditionGroup, TransitionSyncV2MinimalIndepdendentsSolutionFinder.IndependentGroupSolution>
) {
    fun checkToken(inputArc: InputArcWrapper, tokenWrapper: TokenWrapper): Boolean {
        val tokenConformsToAllEntries = inputArc.underConditions.all { condition ->
            tokenWrapper in presolution[inputArc.independentGroup]!!.sharedEntries[condition]!!
        }

        return tokenConformsToAllEntries
    }
}