package simulation.time

import model.Time
import model.Transition
import model.time.IntervalFunction
import kotlin.random.Random

class NextTransitionOccurenceAllowedTimeSelector(
    private val random: Random,
    private val  intervalFunction: IntervalFunction,
) {
    fun get(interval : IntRange): Time {
        return interval.random(random = random)
    }

    fun getNewNextOccurrenceTime(transition: Transition): Time {
        return get(intervalFunction[transition].pauseBeforeNextOccurence)
    }
}
