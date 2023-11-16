package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
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
    fun consumeTokenBuffer(): OutputMissingTokensGenerator
}

interface OutputMissingTokensGenerator {
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
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta
    ): TokenBuffer?

    fun getInputArcs(): Collection<Arc>
    fun getTokenAmountComingThroughArc(arc: Arc): Int

    interface BatchGroupingStrategy {
        fun findTokenBatchForOTypeAndArc(
            sourceTokenBatches: List<TokenBatch>,
            objectTypeId: ObjectTypeId,
            arcMeta: ArcMeta
        ): TokenBatch?
    }
}
