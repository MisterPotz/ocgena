package ru.misterpotz.ocgena.simulation.config

import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.structure.OcNetInstance

class SimulationConfig(
    val templateOcNet: OcNetInstance<*>,
    val initialMarking: MarkingScheme,
    val timeoutSec: Long?,
    val randomSeed: Int?,
    val useRandom: Boolean = true,
    val nodeToLabelRegistry: NodeToLabelRegistry,
//    val objectTokenGenerator: ObjectTokenGenerator,
    val generationConfig: GenerationConfig?,
) {
    val ocNetType: OcNetType
        get() = templateOcNet.ocNetType
}