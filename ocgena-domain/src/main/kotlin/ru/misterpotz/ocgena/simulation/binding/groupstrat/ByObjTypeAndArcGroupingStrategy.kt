package ru.misterpotz.ocgena.simulation.binding.groupstrat

import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroup
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import javax.inject.Inject

class ByObjTypeAndArcGroupingStrategy @Inject constructor(): TokenGroupedInfo.TokenGroupingStrategy {
    override fun findTokenBatchForOTypeAndArc(
        sourceTokenGroups: List<TokenGroup>,
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta
    ): TokenGroup? {
        return sourceTokenGroups.find { tokenGroup: TokenGroup ->
            tokenGroup.objectTypeId == objectTypeId && arcMeta == tokenGroup.arcMeta
        }
    }
}