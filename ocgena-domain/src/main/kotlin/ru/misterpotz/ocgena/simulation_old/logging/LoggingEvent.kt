package ru.misterpotz.ocgena.simulation_old.logging

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.Time

@Serializable
data class LoggingEvent(
    val step : Long,
    val logEvent: LogEvent,
    val simTime : Time,
    val currentMarking : ImmutablePlaceToObjectMarking? = null,
    val lockedTokens : ImmutablePlaceToObjectMarking? = null,
    val unlockedTokens : ImmutablePlaceToObjectMarking? = null
)
