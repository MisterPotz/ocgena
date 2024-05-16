package ru.misterpotz.ocgena.simulation_old.logging.loggers

import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarkingDelta
import ru.misterpotz.ocgena.simulation_old.logging.LogConfiguration
import javax.inject.Inject

class TransitionEndLoggerDelegate @Inject constructor(
    private val logConfigurationx: LogConfiguration
) {
    private val accumulatedUnlockedTokens: PlaceToObjectMarking = PlaceToObjectMarking()

    fun applyDelta(lockedTokensDelta: ImmutablePlaceToObjectMarking) {
        accumulatedUnlockedTokens.plus(lockedTokensDelta as PlaceToObjectMarkingDelta)
    }

    fun getAccumulatedChange(): ImmutablePlaceToObjectMarking {
        return accumulatedUnlockedTokens.toImmutable()
    }

    fun clear() {
        accumulatedUnlockedTokens.clear()
    }
}
