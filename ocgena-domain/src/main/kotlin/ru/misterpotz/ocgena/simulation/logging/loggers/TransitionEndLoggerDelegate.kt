package ru.misterpotz.ocgena.simulation.logging.loggers

import ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation.logging.LogConfiguration
import javax.inject.Inject

class TransitionEndLoggerDelegate @Inject constructor(
    private val logConfigurationx: LogConfiguration
) {
    private val accumulatedUnlockedTokens: PlaceToObjectMarking = PlaceToObjectMarking()

    fun applyDelta(lockedTokensDelta: ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking) {
        accumulatedUnlockedTokens.plus(lockedTokensDelta)
    }

    fun getAccumulatedChange(): ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking {
        return accumulatedUnlockedTokens.toImmutable()
    }

    fun clear() {
        accumulatedUnlockedTokens.clear()
    }
}
