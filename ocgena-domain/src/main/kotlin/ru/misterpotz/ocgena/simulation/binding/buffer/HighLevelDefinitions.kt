package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.TokenSet
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import java.util.*

interface TokenGroupCreator {
    fun group(fromPlacesMarking: SparseTokenBunch): TokenGroupedInfoImpl
    fun createTokensConsumer(): OutputTokensBufferConsumer
}

interface OutputTokensBufferConsumer {
    fun transitionBufferInfo(): TokenGroupedInfo
    fun consumeTokenBuffer(): OutputMissingTokensFiller
}

interface OutputMissingTokensFiller {
    val currentPlaceToObjectMarking : PlaceToObjectMarking
    fun generateMissingTokens(): ImmutablePlaceToObjectMarking
}

class TokenGroup(
    val sortedSet: SortedSet<ObjectTokenId>,
    val objectTypeId: ObjectTypeId,
    val arcMeta: ArcMeta,
) {
    override fun toString(): String {
        return String.format(
            "%-5s | %-20s | size %s",
            objectTypeId,
            arcMeta,
            sortedSet.size
        )
    }

    fun shortString() : String {
        return String.format(
            "%s|%s|%s",
            objectTypeId,
            arcMeta.shortString(),
            sortedSet.size
        )
    }
}

interface TokenGroupedInfo {
    val transition: Transition
    val tokenGroupingStrategy: TokenGroupingStrategy
    fun getTokenSetBy(
        toPlaceObjectTypeId: ObjectTypeId,
        outputArcMeta: ArcMeta
    ): TokenSet?

    interface TokenGroupingStrategy {
        fun findTokenBatchForOTypeAndArc(
            sourceTokenGroups: List<TokenGroup>,
            objectTypeId: ObjectTypeId,
            arcMeta: ArcMeta
        ): TokenGroup?
    }
}
