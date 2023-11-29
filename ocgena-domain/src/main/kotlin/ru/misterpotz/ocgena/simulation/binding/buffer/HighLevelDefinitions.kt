package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.TokenBuffer
import java.util.*

interface LockedTokenBufferizer {
    fun bufferize(fromPlacesMarking: ImmutablePlaceToObjectMarking)
    fun createTokensConsumer(): OutputTokensBufferConsumer
}

interface OutputTokensBufferConsumer {
    fun transitionBufferInfo(): TransitionBufferInfo
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

interface TransitionBufferInfo {
    val transition: Transition
    val batchGroupingStrategy: BatchGroupingStrategy
    fun getBatchBy(
        toPlaceObjectTypeId: ObjectTypeId,
        outputArcMeta: ArcMeta
    ): TokenBuffer?

//    fun getInputArcs(): Collection<Arc>
//    fun getTokenAmountComingThroughInputArc(arc: Arc): Int

    interface BatchGroupingStrategy {
        fun findTokenBatchForOTypeAndArc(
            sourceTokenBatches: List<TokenBatch>,
            objectTypeId: ObjectTypeId,
            arcMeta: ArcMeta
        ): TokenBatch?
    }
}
