package ru.misterpotz.simulation.state

import kotlinx.serialization.Serializable
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import model.SerializableActiveTransitionMarking
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.transition.SerializableTransitionOccurrenceAllowedTimes

@Serializable
data class SerializableState(
    val tMarking: SerializableActiveTransitionMarking,
    val pMarking: ImmutableObjectMarking,
    val tMinTimes: SerializableTransitionOccurrenceAllowedTimes,
//    val intervalFunction: SerializableIntervalFunction,
) : SimulatableComposedOcNet.SerializableState {

}