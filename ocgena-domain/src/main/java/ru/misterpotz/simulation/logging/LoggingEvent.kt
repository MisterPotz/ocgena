package ru.misterpotz.simulation.logging

import kotlinx.serialization.Serializable
import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.Time

@Serializable
data class LoggingEvent(
    val step : Long,
    val logEvent: LogEvent,
    val simTime : Time,
    val currentMarking : ImmutableObjectMarking? = null,
    val lockedTokens : ImmutableObjectMarking? = null,
    val unlockedTokens : ImmutableObjectMarking? = null
)