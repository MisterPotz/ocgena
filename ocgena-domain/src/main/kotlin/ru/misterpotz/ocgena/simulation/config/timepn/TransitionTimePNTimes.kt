package ru.misterpotz.ocgena.simulation.config.timepn

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.simulation.config.original.Duration

@Serializable
data class TransitionTimePNTimes(
    @Contextual @SerialName("duration")
    val duration: Duration,
) {
    val earlyFiringTime
        get() = duration.intRange.first
    val latestFiringTime
        get() = duration.intRange.last
}