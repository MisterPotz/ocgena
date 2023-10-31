package model.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

fun getCurrentTime(): Long = System.currentTimeMillis()

fun getSmartCurrentTime() : Long {
    return Clock.System.now().toEpochMilliseconds()
}

fun formatMillisToUTCString(timestamp: Duration): String {
    val instant = Instant.fromEpochMilliseconds(timestamp.inWholeMilliseconds)
    return instant.toLocalDateTime(TimeZone.UTC).toString()
}
