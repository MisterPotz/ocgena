package model.time

import java.time.Instant

actual fun getCurrentTime(): Long {
    return Instant.now().toEpochMilli()

}
