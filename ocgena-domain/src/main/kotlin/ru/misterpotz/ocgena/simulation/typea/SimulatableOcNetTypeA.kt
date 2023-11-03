package simulation.typea

import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation.config.IntervalFunction
import ru.misterpotz.ocgena.ocnet.StaticCoreOcNetScheme
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityTypeARegistry
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.state.State

class SimulatableOcNetTypeA(
    override val ocNetScheme: StaticCoreOcNetScheme,
    override val arcMultiplicity: ArcToMultiplicityTypeARegistry,
    override val intervalFunction: IntervalFunction,
) : SimulatableComposedOcNet<SimulatableOcNetTypeA> {
    override val ocNetType: OcNetType = OcNetType.AALST

    override fun createInitialState(): SimulatableComposedOcNet.State {
        return State()
    }

    override fun fullCopy(): SimulatableOcNetTypeA {
        return SimulatableOcNetTypeA(
            ocNetScheme = ocNetScheme,
            arcMultiplicity = arcMultiplicity,
            intervalFunction = intervalFunction,
        )
    }
}
