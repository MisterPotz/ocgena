package simulation

import config.TimeRange
import ru.misterpotz.marking.objects.Time
import javax.inject.Inject
import kotlin.random.Random

class TokenGenerationTimeSelector @Inject constructor(
    private val random: Random?,
) {
    fun get(interval: TimeRange): Time {
        if (random == null) return interval.start.toLong()
        return (interval.start..interval.end).random(random = random).toLong()
    }
}
