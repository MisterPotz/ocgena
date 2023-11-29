package ru.misterpotz.ocgena.registries.typel

import ru.misterpotz.expression.paramspace.VariableParameterSpace
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.InputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityNormalDelegateTypeA
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionBufferInfo
import javax.inject.Inject

class ArcToMultiplicityVariableDelegateTypeL @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val arcToMultiplicityNormalDelegateTypeA: ArcToMultiplicityNormalDelegateTypeA,
    ocNet: OCNet,
) : ArcsMultiplicityDelegate() {
    private val placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry
    override fun transitionInputMultiplicity(arc: Arc): InputArcMultiplicity {
        require(arc is VariableArc)

        // use fallback
        arc.variableName
            ?: return arcToMultiplicityNormalDelegateTypeA.transitionInputMultiplicity(arc)

        val realTokenAmount = objectTokenRealAmountRegistry.getRealAmountAt(arc.tailNodeId!!)

        return InputArcMultiplicityValue(
            sourceNodeHasEnoughTokens = (realTokenAmount) > 1,
            requiredTokenAmount = realTokenAmount // require as much tokens as there are
        )
    }

    override fun transitionOutputMultiplicity(
        transitionBufferInfo: TransitionBufferInfo,
        arc: Arc
    ): OutputArcMultiplicity {
        require(arc is VariableArc)

        val targetPlace = arc.arrowNodeId!!
        val objectTypeId = placeToObjectTypeRegistry[targetPlace]

        val variableName = arc.variableName
            ?: return arcToMultiplicityNormalDelegateTypeA
                .transitionOutputMultiplicity(transitionBufferInfo, arc)

        val sourceBatch = transitionBufferInfo.getBatchBy(
            toPlaceObjectTypeId = objectTypeId,
            outputArcMeta = arc.arcMeta
        )!!
        val totalAvailableTokensForArc = sourceBatch.size

        // calculate the amount of tokens needed for this arc
        val requiredTokensAmount = arc.mathNode!!.evaluate(
            parameterSpace =
            VariableParameterSpace(
                variableName to totalAvailableTokensForArc.toDouble()
            )
        )

        val requiredTokenAmount = roundUpIfNeeded(requiredTokensAmount)

        return OutputArcMultiplicityValue(
            tokenBuffer = sourceBatch,
            requiredTokenAmount = requiredTokenAmount,
        )
    }

    private fun roundUpIfNeeded(value: Double): Int {
        return if (value % 1.0 != 0.0) {
            kotlin.math.ceil(value).toInt()
        } else {
            value.toInt()
        }
    }
}
