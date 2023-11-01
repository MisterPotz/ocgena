package simulation.binding

import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.Time
import ru.misterpotz.model.atoms.TransitionId
import kotlin.math.abs

class EnabledBindingWithTokens(
    val transition: TransitionId,
    val involvedObjectTokens: ImmutableObjectMarking,
) {
    val synchronizationTime = calculateSynchronizationTime()

    private fun calculateSynchronizationTime(): Time {
        val allTokens = involvedObjectTokens.allTokens()

        val minTime = allTokens.minBy { it.ownPathTime }.ownPathTime
        val maxTime = allTokens.maxBy { it.ownPathTime }.ownPathTime
        return abs(maxTime - minTime)
    }
}
