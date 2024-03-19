package ru.misterpotz.ocgena.simulation.config.timepn

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.simulation.config.original.Duration
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePnTransitionData

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

fun IntRange.toTimePNTimes(): TransitionTimePNTimes {
    return TransitionTimePNTimes(Duration(this))
}

fun LongRange.toTimePNData(clock: Long = 0): TimePnTransitionData {
    return TimePnTransitionData(clock, start, endInclusive)
}