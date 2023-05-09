package model.time

data class TransitionTimes(
    val duration : IntRange,
    val pauseBeforeNextOccurence : IntRange,
) {
    val earlyFiringTime
        get() = duration.first
    val latestFiringTime
        get() = duration.last
}
