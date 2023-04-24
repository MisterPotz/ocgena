package simulation.binding

import model.ObjectMarking
import model.Time
import model.Transition
import kotlin.math.abs

class EnabledBindingWithTokens(
    val transition: Transition,
    val involvedObjectTokens: ObjectMarking,
) {
    val synchronizationTime = calculateSynchronizationTime()

    private fun calculateSynchronizationTime(): Time {
        val allTokens = involvedObjectTokens.allTokens()

        val minTime = allTokens.minBy { it.ownPathTime }.ownPathTime
        val maxTime = allTokens.maxBy { it.ownPathTime }.ownPathTime
        return abs(maxTime - minTime)
    }
}
