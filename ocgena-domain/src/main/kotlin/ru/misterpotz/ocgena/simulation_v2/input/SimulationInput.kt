package ru.misterpotz.ocgena.simulation_v2.input

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

@Serializable
data class SimulationInput(
    val places: Map<PetriAtomId, PlaceSetting> = mapOf(),
    val transitions: Map<PetriAtomId, TransitionSetting> = mapOf(),
    @SerialName("default_transition_eft_lft")
    val defaultEftLft: Interval? = null,
    @SerialName("default_token_gen_interval")
    val defaultTokenGenerationInterval: Interval? = null,
    @SerialName("seed")
    val randomSeed: Int? = null,
    @SerialName("logging")
    val loggingEnabled: Boolean? = null
)

@Serializable
data class PlaceSetting(
    val label: String? = null,
    @SerialName("initial_tokens_amount") val initialTokens: Int? = null,
    @SerialName("generation_interval") val generationInterval: Interval? = null,
    @SerialName("generate_tokens_target") val generateTokensTarget: Int? = null
)

@Serializable
data class TransitionSetting(
    @SerialName("eft_lft")
    val eftLft: Interval? = null,
    val synchronizedArcGroups: List<SynchronizedArcGroup>? = null
)

@Serializable
data class SynchronizedArcGroup(
    @SerialName("sync_transition")
    val syncTransition: String,
    @SerialName("arcs_from")
    val arcsFromPlaces: List<PetriAtomId>
)

@Serializable(with = Interval.Companion.DefaultSerializer::class)
data class Interval(@Contextual val values: IntRange) {
    companion object {
        class DefaultSerializer : KSerializer<Interval> {
            @OptIn(ExperimentalSerializationApi::class)
            override val descriptor: SerialDescriptor = listSerialDescriptor<Int>()
            val listSerializer = ListSerializer(Int.serializer())

            override fun deserialize(decoder: Decoder): Interval {
                val list = decoder.decodeSerializableValue(listSerializer)
                return Interval(IntRange(list.first(), list.getOrElse(1) { list.first() }))
            }

            override fun serialize(encoder: Encoder, value: Interval) {
                val list = listOf(value.values.first, value.values.last)
                encoder.encodeSerializableValue(listSerializer, list)
            }
        }
    }
}
