package ru.misterpotz.ocgena.simulation.config.original

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransitionInstanceTimes(
    @Contextual @SerialName("duration")
    val duration: Duration,
    @Contextual @SerialName("until_next_permit")
    val timeUntilNextInstanceIsAllowed: TimeUntilNextInstanceIsAllowed,
) {
    val earlyFiringTime
        get() = duration.intRange.first
    val latestFiringTime
        get() = duration.intRange.last

    companion object {

    }
}

fun IntRange.toDuration(): Duration {
    return Duration(this)
}

fun IntRange.withUntilNext(intRange: IntRange): TransitionInstanceTimes {
    return TransitionInstanceTimes(
        Duration(this),
        timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(intRange)
    )
}

@Serializable
data class Duration(
    @Contextual
    val intRange: IntRange,
)

@Serializable
data class TimeUntilNextInstanceIsAllowed(
    @Contextual
    val intRange: IntRange,
)