package ru.misterpotz.ocgena.simulation.semantics

import kotlinx.serialization.Serializable

enum class SimulationSemanticsType {
    ORIGINAL,
    SIMPLE_TIME_PN,
}

@Serializable
class SimulationSemantics(
    val type: SimulationSemanticsType = SimulationSemanticsType.ORIGINAL
)