package ru.misterpotz.simulation.logging

import kotlinx.serialization.Serializable
import ru.misterpotz.model.ImmutableObjectMarking
import model.Time

@Serializable
data class LoggingEvent(
    val step : Long,
    val logEvent: LogEvent,
    val simTime : Time,
    val currentMarking : ImmutableObjectMarking
)