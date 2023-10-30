package ru.misterpotz.simulation.transition

import kotlinx.serialization.Serializable
import ru.misterpotz.model.marking.Time
import model.TransitionId

@Serializable
class SerializableTransitionOccurrenceAllowedTimes(val transitionsToNextTimes : Map<TransitionId, Time>)