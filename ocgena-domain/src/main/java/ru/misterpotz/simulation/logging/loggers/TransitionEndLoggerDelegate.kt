package ru.misterpotz.simulation.logging.loggers

import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.ObjectMarking
import ru.misterpotz.simulation.logging.LogConfiguration
import javax.inject.Inject

class TransitionEndLoggerDelegate @Inject constructor(
    private val logConfigurationx: LogConfiguration
) {
    private val accumulatedUnlockedTokens: ObjectMarking = ObjectMarking()

    fun applyDelta(lockedTokensDelta: ImmutableObjectMarking) {
        accumulatedUnlockedTokens.plus(lockedTokensDelta)
    }

    fun getAccumulatedChange(): ImmutableObjectMarking {
        return accumulatedUnlockedTokens.toImmutable()
    }

    fun clear() {
        accumulatedUnlockedTokens.clear()
    }
}
