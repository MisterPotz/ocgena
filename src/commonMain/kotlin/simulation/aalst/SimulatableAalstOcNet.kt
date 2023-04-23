package simulation.aalst

import model.IntervalFunction
import model.StaticCoreOcNet
import model.aalst.StaticArcMultiplicity
import simulation.SimulatableComposedOcNet
import simulation.State

class SimulatableAalstOcNet(
    override val coreOcNet: StaticCoreOcNet,
    override val arcMultiplicity: StaticArcMultiplicity,
    override val intervalFunction: IntervalFunction,
) : SimulatableComposedOcNet<SimulatableAalstOcNet> {
    override fun createInitialState(): SimulatableComposedOcNet.State {
        return State()
    }

    override fun fullCopy(): SimulatableAalstOcNet {
        return SimulatableAalstOcNet(
            coreOcNet = coreOcNet,
            arcMultiplicity = arcMultiplicity,
            intervalFunction = intervalFunction,
        )
    }
}
