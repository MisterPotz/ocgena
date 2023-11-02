package ru.misterpotz.ocgena.simulation.api.interactors

import ru.misterpotz.marking.objects.ObjectToken
import ru.misterpotz.marking.objects.ObjectTokenId
import java.util.*

interface TokenSelectionInteractor {
    fun getTokensFromSet(
        set: SortedSet<ObjectTokenId>,
        amount: Int
    ): SortedSet<ObjectTokenId>

    fun shuffleTokens(tokens: List<ObjectTokenId>): List<ObjectTokenId>
    fun shuffleTokens(tokens : SortedSet<ObjectTokenId>) : SortedSet<ObjectTokenId>
}