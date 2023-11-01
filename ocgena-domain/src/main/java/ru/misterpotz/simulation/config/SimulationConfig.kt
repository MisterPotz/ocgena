package ru.misterpotz.simulation.config

import config.GenerationConfig
import model.LabelMapping
import model.OcNetType
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.plain.PlainMarking
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.token_generation.ObjectTokenGenerator

data class SimulationConfig(
    val templateOcNet: SimulatableComposedOcNet<*>,
    val initialMarking: PlainMarking,
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
