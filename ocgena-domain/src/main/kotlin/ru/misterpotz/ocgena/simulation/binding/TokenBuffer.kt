package ru.misterpotz.ocgena.simulation.binding

import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenBatch
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionGroupedTokenInfo
import java.util.*

typealias TokenGroup = SortedSet<ObjectTokenId>


class TokenBatchList(
    private val tokenGroupingStrategy: TransitionGroupedTokenInfo.TokenGroupingStrategy
) {
    private val tokenBatches: MutableList<TokenBatch> = mutableListOf()

    fun addTokens(
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta,
        sortedSet: SortedSet<ObjectTokenId>
    ) {
        val similarTokenBatch = tokenGroupingStrategy.findTokenBatchForOTypeAndArc(
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
    ): TokenGroup? {
        val batch = tokenGroupingStrategy.findTokenBatchForOTypeAndArc(
            tokenBatches,
            objectTypeId,
            arcMeta
        )
        return batch?.sortedSet
    }
}