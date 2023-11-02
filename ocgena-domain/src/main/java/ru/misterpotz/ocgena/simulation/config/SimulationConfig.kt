package ru.misterpotz.ocgena.simulation.config

import config.GenerationConfig
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.marking.plain.PlainMarking
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.token_generation.ObjectTokenGenerator

data class SimulationConfig(
    val templateOcNet: SimulatableComposedOcNet<*>,
    val initialMarking: PlainMarking,
    val timeoutSec: Long?,
    val randomSeed: Int?,
    val useRandom: Boolean = true,
    val nodeToLabelRegistry: NodeToLabelRegistry,
    val objectTokenGenerator: ObjectTokenGenerator,
    val generationConfig: GenerationConfig?,
) {
    val ocNetType: OcNetType
        get() = templateOcNet.ocNetType
}
