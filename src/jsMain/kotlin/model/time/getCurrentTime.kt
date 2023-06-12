package model.time

import kotlin.js.Date

actual fun getCurrentTime(): Long {
    return Date().getTime().toLong()
}
