package ru.misterpotz.ocgena.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.misterpotz.ocgena.simulation_old.config.original.Duration
import ru.misterpotz.ocgena.simulation_old.config.Period
import ru.misterpotz.ocgena.simulation_old.config.original.TimeUntilNextInstanceIsAllowed

class IntRangeSerializer(private val name : String) : KSerializer<IntRange> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("IntRange") {
        element<List<Int>>(name)
    }

    override fun serialize(encoder: Encoder, value: IntRange) {
        val array = listOf(value.first, value.last)
        encoder.encodeSerializableValue(ListSerializer(Int.serializer()), array)
    }

    override fun deserialize(decoder: Decoder): IntRange {
        val array = decoder.decodeSerializableValue(ListSerializer(Int.serializer()))
        return IntRange(array[0], array[1])
    }
}

class DurationSerializer(private val name : String) : KSerializer<Duration> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Duration") {
        element<List<Int>>(name)
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        val array = listOf(value.intRange.first, value.intRange.last)
        encoder.encodeSerializableValue(ListSerializer(Int.serializer()), array)
    }

    override fun deserialize(decoder: Decoder): Duration {
        val array = decoder.decodeSerializableValue(ListSerializer(Int.serializer()))
        return Duration(IntRange(array[0], array[1]))
    }
}

class TimeUntilNextInstanceIsAllowedSerializer(private val name : String) : KSerializer<TimeUntilNextInstanceIsAllowed> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TimeUntilNextInstanceIsAllowed") {
        element<List<Int>>(name)
    }

    override fun serialize(encoder: Encoder, value: TimeUntilNextInstanceIsAllowed) {
        val array = listOf(value.intRange.first, value.intRange.last)
        encoder.encodeSerializableValue(ListSerializer(Int.serializer()), array)
    }

    override fun deserialize(decoder: Decoder): TimeUntilNextInstanceIsAllowed {
        val array = decoder.decodeSerializableValue(ListSerializer(Int.serializer()))
        return TimeUntilNextInstanceIsAllowed(IntRange(array[0], array[1]))
    }
}


class PeriodSerializer(private val name : String) : KSerializer<Period> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Period") {
        element<List<Int>>(name)
    }

    override fun serialize(encoder: Encoder, value: Period) {
        val array = listOf(value.intRange.first, value.intRange.last)
        encoder.encodeSerializableValue(ListSerializer(Int.serializer()), array)
    }

    override fun deserialize(decoder: Decoder): Period {
        val array = decoder.decodeSerializableValue(ListSerializer(Int.serializer()))
        return Period(IntRange(array[0], array[1]))
    }
}
