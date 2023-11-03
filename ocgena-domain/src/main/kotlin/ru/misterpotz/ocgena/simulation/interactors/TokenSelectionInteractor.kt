package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.simulation.ObjectTokenId
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

interface TokenSelectionInteractor {
    fun getTokensFromSet(
        set: SortedSet<ObjectTokenId>,
        amount: Int
    ): SortedSet<ObjectTokenId>

    fun shuffleTokens(tokens: List<ObjectTokenId>): List<ObjectTokenId>
    fun shuffleTokens(tokens : SortedSet<ObjectTokenId>) : SortedSet<ObjectTokenId>
}

class TokenSelectionInteractorImpl @Inject constructor(
    private val random: Random?,
    private val repeatabilityInteractor: RepeatabilityInteractor,
) : TokenSelectionInteractor {
    override fun getTokensFromSet(set: SortedSet<ObjectTokenId>, amount: Int): SortedSet<ObjectTokenId> {
        return random?.let {
            set.shuffled(random = it)
                .take(amount)
                .toSortedSet()
        } ?: set
            .take(amount)
            .toSortedSet()
    }

    override fun shuffleTokens(tokens: List<ObjectTokenId>): List<ObjectTokenId> {
        if (random == null) return repeatabilityInteractor.sortTokens(tokens)
        return repeatabilityInteractor.sortTokens(tokens).shuffled(random)
    }

    override fun shuffleTokens(tokens: SortedSet<ObjectTokenId>): SortedSet<ObjectTokenId> {
        if (random == null) return tokens

        return tokens.shuffled(random).toSortedSet()
    }
}
