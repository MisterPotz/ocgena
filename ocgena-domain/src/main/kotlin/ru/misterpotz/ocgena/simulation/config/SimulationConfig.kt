package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry

@Serializable
data class SimulationConfig(
    @Contextual
    val ocNet: OCNet,
    val initialMarking: MarkingScheme,
    @Contextual
    val transitionInstancesTimesSpec: TransitionInstancesTimesSpec,
    val randomSeed: Int?,
    val nodeToLabelRegistry: NodeToLabelRegistry = NodeToLabelRegistry(),
    val tokenGeneration: TokenGenerationConfig?,
    val ocNetType: OcNetType,
)
