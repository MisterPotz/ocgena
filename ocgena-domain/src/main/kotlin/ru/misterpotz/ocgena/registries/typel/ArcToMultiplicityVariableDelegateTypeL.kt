package ru.misterpotz.ocgena.registries.typel

import ru.misterpotz.expression.paramspace.VariableParameterSpace
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicityValue
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityNormalDelegateTypeA
import ru.misterpotz.ocgena.simulation.typea.TransitionBufferInfo
import javax.inject.Inject

class ArcToMultiplicityVariableDelegateTypeL @Inject constructor(
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val arcToMultiplicityNormalDelegateTypeA: ArcToMultiplicityNormalDelegateTypeA,
    private val ocNet: OCNet,
) : ArcsMultiplicityDelegate() {
    private val placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry
    override fun transitionInputMultiplicity(arc: Arc): ArcMultiplicity {
        require(arc is VariableArc)

        // use fallback
        arc.variableName
            ?: return arcToMultiplicityNormalDelegateTypeA.transitionInputMultiplicity(arc)

        val realTokenAmount = objectTokenRealAmountRegistry.getRealAmountAt(arc.tailNodeId!!)

        return ArcMultiplicityValue(
            sourceNodeHasEnoughTokens = (realTokenAmount) > 1,
            requiredTokenAmount = realTokenAmount // require as much tokens as there are
        )
    }

    override fun transitionOutputMultiplicity(transitionBufferInfo: TransitionBufferInfo, arc: Arc): ArcMultiplicity {
        require(arc is VariableArc)

        val variableName = arc.variableName
            ?: return arcToMultiplicityNormalDelegateTypeA.transitionOutputMultiplicity(transitionBufferInfo, arc)
        val placeId = arc.arrowNodeId!!
        val objectTypeId = placeToObjectTypeRegistry[placeId]

        val inputArcForTheVariable = transitionBufferInfo.getInputArcs().find {
            (it is VariableArc) && it.variableName == variableName
        }

        require(inputArcForTheVariable != null) {
            "output arc with variable $variableName has no matched input arc"
        }
        val variableValue = transitionBufferInfo.getItemsPerArc(inputArcForTheVariable)

        val thisArcExpressionEvaluation = arc.mathNode!!.evaluate(
            parameterSpace = VariableParameterSpace(variableName to variableValue.toDouble())
        )
        val itemsPerType = transitionBufferInfo.getBatchBy(objectTypeId).size

        val requiredTokenAmount = roundUpIfNeeded(thisArcExpressionEvaluation)

        val bufferHasEnoughTokens = requiredTokenAmount <= itemsPerType

        return ArcMultiplicityValue(
            sourceNodeHasEnoughTokens = bufferHasEnoughTokens,
            requiredTokenAmount = requiredTokenAmount
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
