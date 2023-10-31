package ru.misterpotz.simulation.logging.loggers

import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.ObjectMarking
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
