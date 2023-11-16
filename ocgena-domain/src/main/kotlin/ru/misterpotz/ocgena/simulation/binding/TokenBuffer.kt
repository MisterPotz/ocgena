package ru.misterpotz.ocgena.simulation.binding

import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenBatch
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionBufferInfo
import java.util.*

typealias TokenBuffer = SortedSet<ObjectTokenId>


class TokenBatchList(
    private val batchGroupingStrategy: TransitionBufferInfo.BatchGroupingStrategy
) {
    private val tokenBatches: MutableList<TokenBatch> = mutableListOf()

    fun addTokens(
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta,
        sortedSet: SortedSet<ObjectTokenId>
    ) {
        val similarTokenBatch = batchGroupingStrategy.findTokenBatchForOTypeAndArc(
            tokenBatches,
            objectTypeId,
            arcMeta
        )

        @Suppress("IfThenToElvis")
        if (similarTokenBatch != null) {
            similarTokenBatch.sortedSet.addAll(sortedSet)
        } else {
            tokenBatches.add(
                TokenBatch(
                    sortedSet = sortedSet,
                    objectTypeId = objectTypeId,
                    arcMeta = arcMeta
                )
            )
        }
    }

    fun getBatchBy(
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta
    ): TokenBuffer? {
        val batch = batchGroupingStrategy.findTokenBatchForOTypeAndArc(
            tokenBatches,
            objectTypeId,
            arcMeta
        )
        return batch?.sortedSet
    }
}