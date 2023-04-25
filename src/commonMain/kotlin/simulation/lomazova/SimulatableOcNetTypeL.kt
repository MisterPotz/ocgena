package simulation.lomazova

import model.time.IntervalFunction
import model.StaticCoreOcNet
import model.lomazova.ExpressionArcMultiplicity
import simulation.SimulatableComposedOcNet

class SimulatableOcNetTypeL(
    override val coreOcNet: StaticCoreOcNet,
    override val arcMultiplicity: ExpressionArcMultiplicity,
    override val intervalFunction: IntervalFunction,
) : SimulatableComposedOcNet<SimulatableOcNetTypeL> {
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
