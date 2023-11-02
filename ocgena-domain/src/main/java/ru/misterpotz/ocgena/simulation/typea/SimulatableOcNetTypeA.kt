package simulation.typea

import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import model.time.IntervalFunction
import model.StaticCoreOcNet
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityTypeARegistry
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.state.State

class SimulatableOcNetTypeA(
    override val coreOcNet: StaticCoreOcNet,
    override val arcMultiplicity: ArcToMultiplicityTypeARegistry,
    override val intervalFunction: IntervalFunction,
) : SimulatableComposedOcNet<SimulatableOcNetTypeA> {
    override val ocNetType: OcNetType = OcNetType.AALST

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
