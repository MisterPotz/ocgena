package simulation

import config.TimeRange
import ru.misterpotz.model.marking.Time
import javax.inject.Inject
import kotlin.random.Random

class TokenGenerationTimeSelector @Inject constructor(
    private val random: Random?,
) {
    fun get(interval : TimeRange): Time {
        return random?.let {
            (interval.start..interval.end).random(random = it)
        } ?: interval.start
    }
}
