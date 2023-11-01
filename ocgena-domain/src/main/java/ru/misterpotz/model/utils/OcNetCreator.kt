package model.utils

import ru.misterpotz.model.atoms.Arc
import model.OcNetType
import model.StaticCoreOcNet
import model.time.IntervalFunction
import model.typea.ArcMultiplicityTypeA
import model.typel.ArcExpression
import model.typel.ExpressionArcMultiplicity
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import simulation.typea.SimulatableOcNetTypeA
import simulation.typel.SimulatableOcNetTypeL

class OcNetCreator(
    private val coreOcNet: StaticCoreOcNet,
    private val timeIntervalFunction: IntervalFunction
) {
    fun create(ocNetType: OcNetType): SimulatableComposedOcNet<*> {
        return when(ocNetType) {
            OcNetType.AALST -> {
                SimulatableOcNetTypeA(
                    coreOcNet,
                    arcMultiplicity = ArcMultiplicityTypeA(coreOcNet.arcs),
                    intervalFunction = timeIntervalFunction
                )
            }
            OcNetType.LOMAZOVA -> {
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
