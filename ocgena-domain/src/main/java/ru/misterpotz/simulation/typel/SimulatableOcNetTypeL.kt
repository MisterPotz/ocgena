package simulation.typel

import model.OcNetType
import model.time.IntervalFunction
import model.StaticCoreOcNet
import model.typel.ExpressionArcMultiplicity
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet

class SimulatableOcNetTypeL(
    override val coreOcNet: StaticCoreOcNet,
    override val arcMultiplicity: ExpressionArcMultiplicity,
    override val intervalFunction: IntervalFunction,
) : SimulatableComposedOcNet<SimulatableOcNetTypeL> {
    override val ocNetType: OcNetType = OcNetType.LOMAZOVA
    override fun createInitialState(): SimulatableComposedOcNet.State {
        TODO("Not yet implemented")
    }

    override fun fullCopy(): SimulatableOcNetTypeL {
        return SimulatableOcNetTypeL(
            coreOcNet = coreOcNet,
            arcMultiplicity = arcMultiplicity,
            intervalFunction = intervalFunction
        )
    }
}