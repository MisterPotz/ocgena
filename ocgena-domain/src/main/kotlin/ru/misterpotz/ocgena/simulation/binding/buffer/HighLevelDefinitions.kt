package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
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
)

interface TokenGroupedInfo {
    val transition: Transition
    val tokenGroupingStrategy: TokenGroupingStrategy
    fun getTokenSetBy(
        toPlaceObjectTypeId: ObjectTypeId,
        outputArcMeta: ArcMeta
    ): TokenSet?

//    fun getInputArcs(): Collection<Arc>
//    fun getTokenAmountComingThroughInputArc(arc: Arc): Int

    interface TokenGroupingStrategy {
        fun findTokenBatchForOTypeAndArc(
            sourceTokenGroups: List<TokenGroup>,
            objectTypeId: ObjectTypeId,
            arcMeta: ArcMeta
        ): TokenGroup?
    }
}
