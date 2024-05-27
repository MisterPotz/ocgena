package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IndependentMultiConditionGroup
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice

data class ArcToApplicableTokens(val inputArc: InputArcWrapper, val iterator: Iterator<Token>)

fun makeHeteregenousArcTokensIterator(
    transition: TransitionWrapper,
    synchronizationPresolution: Map<IndependentMultiConditionGroup, TransitionSyncV2MinimalIndepdendentsSolutionFinder.IndependentGroupSolution>,
    filteredTokenSlice: TokenSlice,
    currentSolution: TokenSlice?
): List<ArcToApplicableTokens> {
    return transition.inputArcs.map {
        ArcToApplicableTokens(
            it,
            getApplicableTokensIterator(
                it,
                tokenChecker = TokenChecker(synchronizationPresolution),
                filteredTokenSlice = filteredTokenSlice,
                currentSolution = currentSolution
            )
        )
    }
}

fun checkDidNotConsumeTokenAndIsApplicable(
    tokenChecker: TokenChecker,
    inputArc: InputArcWrapper,
    tokenWrapper: TokenWrapper,
    currentSolution: TokenSlice?
): Boolean {
    return (
            currentSolution == null || binarySearchIndex(
                currentSolution.tokensAt(inputArc.fromPlace),
                tokenWrapper
            ) == null
            ) &&
            tokenChecker.checkToken(inputArc, tokenWrapper)
}

fun <T : Comparable<T>> binarySearchIndex(sortedList: List<T>, item: T): Int? {
    var left = 0
    var right = sortedList.size - 1

    while (left <= right) {
        val middle = (left + right) / 2
        val middleItem = sortedList[middle]

        when {
            middleItem == item -> return middle
            middleItem < item -> left = middle + 1
            else -> right = middle - 1
        }
    }
    return null
}

fun getApplicableTokensIterator(
    inputArc: InputArcWrapper,
    tokenChecker: TokenChecker,
    filteredTokenSlice: TokenSlice,
    currentSolution: TokenSlice?
) = iterator {
    if (inputArc.underConditions.isNotEmpty()) {
        for (tokenCandidate in filteredTokenSlice.tokensAt(inputArc.fromPlace)) {
            if (
                checkDidNotConsumeTokenAndIsApplicable(tokenChecker, inputArc, tokenCandidate, currentSolution)
            ) {
                yield(tokenCandidate)
            }
        }
    } else {
        val totalTokens = filteredTokenSlice.tokensAt(inputArc.fromPlace)
        // first try consuming existing tokens
        for (tokenCandidate in totalTokens) {
            if (currentSolution == null || binarySearchIndex(
                    currentSolution.tokensAt(inputArc.fromPlace),
                    tokenCandidate
                ) == null
            ) {
                yield(tokenCandidate)
            }
        }
        // second try consuming existing tokens that are still numbers (not yet entities / unrealized)
        for (tokenIndex in 0..<(filteredTokenSlice.amountAt(inputArc.fromPlace) - totalTokens.size)) {
            yield(UngeneratedToken(inputArc.fromPlace.objectType))
        }
    }
}