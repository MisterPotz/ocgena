package ru.misterpotz.ocgena.simulation.structure

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation.di.SimulationScope
import javax.inject.Inject

@SimulationScope
class SimulatableOcNetInstanceImpl @Inject constructor(
    override val ocNet: OCNet,
    override val state: State,
    override val ocNetType: OcNetType
) : SimulatableOcNetInstance
