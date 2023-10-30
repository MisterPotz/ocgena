package ru.misterpotz.simulation.config

import config.GenerationConfig
import model.LabelMapping
import model.ObjectMarking
import model.OcNetType
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import simulation.ObjectTokenGenerator

data class SimulationConfig(
    val templateOcNet: SimulatableComposedOcNet<*>,
    val initialMarking: ObjectMarking,
    val timeoutSec: Long?,
    val randomSeed: Int?,
    val useRandom: Boolean = true,
    val labelMapping: LabelMapping,
    val objectTokenGenerator: ObjectTokenGenerator,
    val generationConfig: GenerationConfig?,
) {
    val ocNetType: OcNetType
        get() = templateOcNet.ocNetType
}
