package ru.misterpotz.simulation.logging.loggers

import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.ObjectMarking
import ru.misterpotz.simulation.logging.LogConfiguration
import javax.inject.Inject

class TransitionStartLoggerDelegate @Inject constructor(
    private val logConfigurationx: LogConfiguration
) {
    private val accumulatedLockedTokens: ObjectMarking = ObjectMarking()

    fun applyDelta(lockedTokensDelta: ImmutableObjectMarking) {
        accumulatedLockedTokens.plus(lockedTokensDelta)
    }

    fun getAccumulatedChange(): ImmutableObjectMarking {
        return accumulatedLockedTokens.toImmutable()
    }

    fun clear() {
        accumulatedLockedTokens.clear()
    }
}