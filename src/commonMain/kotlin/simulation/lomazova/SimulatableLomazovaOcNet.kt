package simulation.lomazova

import model.IntervalFunction
import model.StaticCoreOcNet
import model.lomazova.ExpressionArcMultiplicity
import simulation.SimulatableComposedOcNet

class SimulatableLomazovaOcNet(
    override val coreOcNet: StaticCoreOcNet,
    override val arcMultiplicity: ExpressionArcMultiplicity,
    override val intervalFunction: IntervalFunction,
) : SimulatableComposedOcNet<SimulatableLomazovaOcNet> {
    override fun createInitialState(): SimulatableComposedOcNet.State {
        TODO("Not yet implemented")
    }

    override fun fullCopy(): SimulatableLomazovaOcNet {
        return SimulatableLomazovaOcNet(
            coreOcNet = coreOcNet,
            arcMultiplicity = arcMultiplicity,
            intervalFunction = intervalFunction
        )
    }
}
