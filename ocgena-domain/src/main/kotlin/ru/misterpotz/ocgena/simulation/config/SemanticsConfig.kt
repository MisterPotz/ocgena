package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SemanticsConfig(
    @SerialName("clock_reset")
    val clockReset: Boolean,
    @SerialName("self_concurrency")
    val selfConcurrency: Boolean
) {
}