package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.TokenGroup
import java.util.*

interface TokenGroupCreator {
    fun group(fromPlacesMarking: ImmutablePlaceToObjectMarking)
    fun createTokensConsumer(): OutputTokensBufferConsumer
}

interface OutputTokensBufferConsumer {
    fun transitionBufferInfo(): TransitionGroupedTokenInfo
    fun consumeTokenBuffer(): OutputMissingTokensFiller
}

interface OutputMissingTokensFiller {
    val currentPlaceToObjectMarking : PlaceToObjectMarking
    fun generateMissingTokens(): ImmutablePlaceToObjectMarking
}

class TokenBatch(
    val sortedSet: SortedSet<ObjectTokenId>,
    val objectTypeId: ObjectTypeId,
    val arcMeta: ArcMeta,
)

interface TransitionGroupedTokenInfo {
    val transition: Transition
    val tokenGroupingStrategy: TokenGroupingStrategy
    fun getGroup(
        toPlaceObjectTypeId: ObjectTypeId,
        outputArcMeta: ArcMeta
    ): TokenGroup?

//    fun getInputArcs(): Collection<Arc>
//    fun getTokenAmountComingThroughInputArc(arc: Arc): Int

    interface TokenGroupingStrategy {
        fun findTokenBatchForOTypeAndArc(
            sourceTokenBatches: List<TokenBatch>,
            objectTypeId: ObjectTypeId,
            arcMeta: ArcMeta
        ): TokenBatch?
    }
}
