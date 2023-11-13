package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArcMetaTypeA
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.typea.TransitionBufferInfo
import javax.inject.Inject

class ArcToMultiplicityVariableDelegateTypeA @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val ocNet: OCNet,
) : ArcsMultiplicityDelegate() {
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry

    override fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity {
        require(arc is VariableArc)

        val realTokenAmount = objectTokenRealAmountRegistry.getRealAmountAt(arc.tailNodeId!!)

        return InputArcMultiplicityValue(
            sourceNodeHasEnoughTokens = (realTokenAmount) > 1,
            requiredTokenAmount = realTokenAmount
        )
    }

    override fun transitionOutputMultiplicity(
        transitionBufferInfo: TransitionBufferInfo,
        arc: Arc
    ): OutputArcMultiplicity {
        require(arc is VariableArc)

        val targetPlace = arc.arrowNodeId!!
        val objectTypeId = placeToObjectTypeRegistry[targetPlace]
        val batchForType = transitionBufferInfo.getBatchBy(
            objectTypeId,
            arcMeta = VariableArcMetaTypeA
        )
        val tokensAtBuffer = batchForType.size

        return OutputArcMultiplicityValue(
            bufferHasEnoughTokens = (tokensAtBuffer) > 1,
            requiredTokenAmount = tokensAtBuffer,
            tokenBuffer = batchForType
        )
    }
}
