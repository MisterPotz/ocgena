package ru.misterpotz.ocgena.simulation_old.logging.loggers

import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarkingDelta
import ru.misterpotz.ocgena.simulation_old.logging.LogConfiguration
import javax.inject.Inject

class TransitionStartLoggerDelegate @Inject constructor(
    private val logConfigurationx: LogConfiguration
) {
    private val accumulatedLockedTokens: PlaceToObjectMarking = PlaceToObjectMarking()

    fun applyDelta(lockedTokensDelta: ImmutablePlaceToObjectMarking) {
        accumulatedLockedTokens.plus(lockedTokensDelta as PlaceToObjectMarkingDelta)
    }

    fun getAccumulatedChange(): ImmutablePlaceToObjectMarking {
        return accumulatedLockedTokens.toImmutable()
    }

    fun clear() {
        accumulatedLockedTokens.clear()
    }
}