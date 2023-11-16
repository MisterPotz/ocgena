package ru.misterpotz.ocgena.simulation.binding.groupstrat

import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenBatch
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionBufferInfo
import javax.inject.Inject

class ByObjTypeGroupingStrategy @Inject constructor() : TransitionBufferInfo.BatchGroupingStrategy {
    override fun findTokenBatchForOTypeAndArc(
        sourceTokenBatches: List<TokenBatch>,
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta
    ): TokenBatch? {
        return sourceTokenBatches.find { tokenBatch: TokenBatch ->
            tokenBatch.objectTypeId == objectTypeId
            // arcmeta doesn't affect
        }
    }
}