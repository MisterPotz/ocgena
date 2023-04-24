package simulation.typea

import model.IntervalFunction
import model.StaticCoreOcNet
import model.aalst.ArcMultiplicityTypeA
import simulation.SimulatableComposedOcNet
import simulation.State

class SimulatableOcNetTypeA(
    override val coreOcNet: StaticCoreOcNet,
    override val arcMultiplicity: ArcMultiplicityTypeA,
    override val intervalFunction: IntervalFunction,
) : SimulatableComposedOcNet<SimulatableOcNetTypeA> {
    override fun createInitialState(): SimulatableComposedOcNet.State {
        return State()
    }

    override fun fullCopy(): SimulatableOcNetTypeA {
        return SimulatableOcNetTypeA(
            coreOcNet = coreOcNet,
            arcMultiplicity = arcMultiplicity,
            intervalFunction = intervalFunction,
        )
    }
}
