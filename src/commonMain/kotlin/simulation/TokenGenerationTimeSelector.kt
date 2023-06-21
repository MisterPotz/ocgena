package simulation

import config.TimeRange
import model.Time
import kotlin.random.Random

class TokenGenerationTimeSelector (
    private val random: Random?,
) {
    fun get(interval : TimeRange): Time {
        return random?.let {
            (interval.start..interval.end).random(random = it)
        } ?: interval.start
    }
}