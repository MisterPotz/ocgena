package model.utils

import model.Arc
import model.OcNetType
import model.StaticCoreOcNet
import model.time.IntervalFunction
import model.typea.ArcMultiplicityTypeA
import model.typel.ArcExpression
import model.typel.ExpressionArcMultiplicity
import simulation.SimulatableComposedOcNet
import simulation.lomazova.SimulatableOcNetTypeL
import simulation.typea.SimulatableOcNetTypeA

class OcNetCreator(
    private val coreOcNet: StaticCoreOcNet,
    private val timeIntervalFunction: IntervalFunction
) {
    fun create(ocNetType: OcNetType): SimulatableComposedOcNet<*> {
        return when(ocNetType) {
            OcNetType.TYPE_A -> {
                SimulatableOcNetTypeA(
                    coreOcNet,
                    arcMultiplicity = ArcMultiplicityTypeA(coreOcNet.arcs),
                    intervalFunction = timeIntervalFunction
                )
            }
            OcNetType.TYPE_L -> {
                SimulatableOcNetTypeL(
                    coreOcNet,
                    arcMultiplicity = object : ExpressionArcMultiplicity {
                        override fun expressionFor(arc: Arc): ArcExpression {
                            TODO("Not yet implemented")
                        }
                    },
                    intervalFunction = timeIntervalFunction
                )
            }
        }
    }
}
