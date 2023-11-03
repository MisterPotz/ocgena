package ru.misterpotz.ocgena.simulation.logging

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking
import ru.misterpotz.marking.objects.Time

@Serializable
data class LoggingEvent(
    val step : Long,
    val logEvent: LogEvent,
    val simTime : Time,
    val currentMarking : ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking? = null,
    val lockedTokens : ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking? = null,
    val unlockedTokens : ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking? = null
)