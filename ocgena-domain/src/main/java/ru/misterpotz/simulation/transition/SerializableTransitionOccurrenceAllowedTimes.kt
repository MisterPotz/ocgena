package ru.misterpotz.simulation.transition

import kotlinx.serialization.Serializable
import model.Time
import model.TransitionId

@Serializable
class SerializableTransitionOccurrenceAllowedTimes(val transitionsToNextTimes : Map<TransitionId, Time>)