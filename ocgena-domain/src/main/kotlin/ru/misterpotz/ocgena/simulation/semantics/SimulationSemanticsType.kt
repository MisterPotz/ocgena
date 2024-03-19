package ru.misterpotz.ocgena.simulation.semantics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SimulationSemanticsType {
    @SerialName("original")
    ORIGINAL,

    @SerialName("time_pn")
    SIMPLE_TIME_PN,
}

@Serializable
data class SimulationSemantics(
    val type: SimulationSemanticsType
)