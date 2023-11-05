package ru.misterpotz.ocgena.simulation.logging

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.simulation.Time

@Serializable
data class LoggingEvent(
    val step : Long,
    val logEvent: LogEvent,
    val simTime : Time,
    val currentMarking : ImmutablePlaceToObjectMarking? = null,
    val lockedTokens : ImmutablePlaceToObjectMarking? = null,
    val unlockedTokens : ImmutablePlaceToObjectMarking? = null
)
