package ru.misterpotz.ocgena.simulation.config

import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.structure.OcNetInstance

class SimulationConfig(
    val ocNetInstance: OcNetInstance,
    val initialMarking: MarkingScheme,
    val randomSeed: Int?,
    val nodeToLabelRegistry: NodeToLabelRegistry,
    val generationConfig: GenerationConfig?,
) {
    val ocNetType: OcNetType
        get() = ocNetInstance.ocNetType
}