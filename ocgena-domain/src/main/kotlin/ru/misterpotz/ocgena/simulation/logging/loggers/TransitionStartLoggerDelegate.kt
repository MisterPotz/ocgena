package ru.misterpotz.ocgena.simulation.logging.loggers

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation.logging.LogConfiguration
import javax.inject.Inject

class TransitionStartLoggerDelegate @Inject constructor(
    private val logConfigurationx: LogConfiguration
) {
    private val accumulatedLockedTokens: PlaceToObjectMarking = PlaceToObjectMarking()

    fun applyDelta(lockedTokensDelta: ImmutablePlaceToObjectMarking) {
        accumulatedLockedTokens.plus(lockedTokensDelta)
    }

    fun getAccumulatedChange(): ImmutablePlaceToObjectMarking {
        return accumulatedLockedTokens.toImmutable()
    }

    fun clear() {
        accumulatedLockedTokens.clear()
    }
}