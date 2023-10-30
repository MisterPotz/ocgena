package model.time

import kotlinx.serialization.Serializable

@Serializable
data class SerializableTransitionTimes(val duration: List<Int>, val minPause: List<Int>)

data class TransitionTimes(
    val duration: IntRange,
    val pauseBeforeNextOccurence: IntRange,
) {
    val serializable by lazy {
        SerializableTransitionTimes(
            duration = listOf(duration.first, duration.last),
            minPause = listOf(pauseBeforeNextOccurence.first, pauseBeforeNextOccurence.last)
        )
    }
    val earlyFiringTime
        get() = duration.first
    val latestFiringTime
        get() = duration.last
}
