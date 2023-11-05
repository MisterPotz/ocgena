package ru.misterpotz.ocgena.simulation.structure

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import javax.inject.Inject

class SimulationableOcNetInstanceImpl @Inject constructor(
    override val ocNet: OCNet,
    override val state: State,
    override val ocNetType: OcNetType
) : SimulatableOcNetInstance
