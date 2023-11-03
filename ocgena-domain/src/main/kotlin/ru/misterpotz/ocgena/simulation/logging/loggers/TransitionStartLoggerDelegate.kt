package ru.misterpotz.ocgena.simulation.logging.loggers

import ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation.logging.LogConfiguration
import javax.inject.Inject

class TransitionStartLoggerDelegate @Inject constructor(
    private val logConfigurationx: LogConfiguration
) {
    private val accumulatedLockedTokens: PlaceToObjectMarking = PlaceToObjectMarking()

    fun applyDelta(lockedTokensDelta: ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking) {
        accumulatedLockedTokens.plus(lockedTokensDelta)
    }

    fun getAccumulatedChange(): ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking {
        return accumulatedLockedTokens.toImmutable()
    }

    fun clear() {
        accumulatedLockedTokens.clear()
    }
}