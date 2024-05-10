package ru.misterpotz.ocgena.simulation_v2.input

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

@Serializable
data class SimulationInput(
    val places: Map<PetriAtomId, PlaceSetting>,
    @SerialName("oc_net_type") val ocNetType: OcNetType,
    val transitions: Map<PetriAtomId, TransitionSetting>,
    @SerialName("default_transition_eft_lft")
    val defaultEftLft: Interval?,
    @SerialName("default_token_gen_interval")
    val defaultTokenGenerationInterval: Interval?,
    @SerialName("seed")
    val randomSeed: Int?
)

@Serializable
data class PlaceSetting(
    val type: String,
    val label: String?,
    @SerialName("initial_tokens_amount") val initialTokens: Int?,
    @SerialName("generation_interval") val generationInterval: Interval?,
    @SerialName("generate_tokens_target") val generateTokensTarget: Int?
)

@Serializable
data class TransitionSetting(
    @SerialName("eft_lft")
    val eftLft: Interval?,
    val synchronizedArcGroups: List<SynchronizedArcGroup>?
)

@Serializable
data class SynchronizedArcGroup(
    @SerialName("common_transition")
    val commonTranstion: String,
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
