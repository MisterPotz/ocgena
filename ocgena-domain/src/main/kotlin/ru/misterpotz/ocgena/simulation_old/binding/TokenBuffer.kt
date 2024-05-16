package ru.misterpotz.ocgena.simulation_old.binding

import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.simulation_old.ObjectTokenId
import ru.misterpotz.ocgena.simulation_old.binding.buffer.TokenGroup
import ru.misterpotz.ocgena.simulation_old.binding.buffer.TokenGroupedInfo
import java.util.*

typealias TokenSet = SortedSet<ObjectTokenId>


class TokenBatchList(
    private val tokenGroupingStrategy: TokenGroupedInfo.TokenGroupingStrategy
) {
    private val tokenGroups: MutableList<TokenGroup> = mutableListOf()

    fun addTokens(
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta,
        sortedSet: SortedSet<ObjectTokenId>
    ) {
        val similarTokenBatch = tokenGroupingStrategy.findTokenBatchForOTypeAndArc(
            tokenGroups,
            objectTypeId,
            arcMeta
        )

        @Suppress("IfThenToElvis")
        if (similarTokenBatch != null) {
            similarTokenBatch.sortedSet.addAll(sortedSet)
        } else {
            tokenGroups.add(
                TokenGroup(
                    sortedSet = sortedSet,
                    objectTypeId = objectTypeId,
                    arcMeta = arcMeta
                )
            )
        }
    }

    fun getTokenSetBy(
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta
    ): TokenSet? {
        val batch = tokenGroupingStrategy.findTokenBatchForOTypeAndArc(
            tokenGroups,
            objectTypeId,
            arcMeta
        )
        return batch?.sortedSet
    }

    override fun toString(): String {
        val str = String.format(
            "tot. groups ${tokenGroups.size}  ||  ${tokenGroups.joinToString("  ##  ") { it.shortString() }}"
        )
        return str
    }
}