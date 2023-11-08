package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry

@Serializable
data class SimulationConfig(
    val ocNet: OCNetStruct,
    val initialMarking: MarkingScheme,
    val transitionInstancesTimesSpec: TransitionInstancesTimesSpec,
    val randomSeed: Int?,
    val nodeToLabelRegistry: NodeToLabelRegistry = NodeToLabelRegistry(),
    val tokenGeneration: TokenGenerationConfig?,
    val ocNetType: OcNetType,
)
