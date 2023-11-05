package ru.misterpotz.ocgena.simulation.config

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry

class SimulationConfig(
    val ocNet: OCNet,
    val initialMarking: MarkingScheme,
    val transitionInstancesTimesSpec: TransitionInstancesTimesSpec,
    val randomSeed: Int?,
    val nodeToLabelRegistry: NodeToLabelRegistry,
    val generationConfig: GenerationConfig?,
    val ocNetType: OcNetType,
)
