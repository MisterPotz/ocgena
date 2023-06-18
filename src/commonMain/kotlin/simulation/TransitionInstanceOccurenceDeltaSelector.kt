package simulation

import model.Time
import model.Transition
import model.time.IntervalFunction
import kotlin.random.Random

class TransitionDurationSelector(
    private val random: Random?,
    private val intervalFunction: IntervalFunction,
) {
    fun newDuration(transition: Transition): Time {
        val times = intervalFunction[transition]
        val duration = random?.let {
            times.duration.random(random)
        } ?: times.duration.first
        return duration
    }
}

class TransitionInstanceOccurenceDeltaSelector(
    private val random: Random?,
    private val  intervalFunction: IntervalFunction,
) {
    fun get(interval : IntRange): Time {
        return random?.let {
            interval.random(random = it)
        } ?: interval.first
    }

    fun getNewNextOccurrenceTime(transition: Transition): Time {
        return get(intervalFunction[transition].pauseBeforeNextOccurence)
    }
}
