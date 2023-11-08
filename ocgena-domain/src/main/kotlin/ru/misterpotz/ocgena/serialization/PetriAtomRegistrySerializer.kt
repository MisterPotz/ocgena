package ru.misterpotz.ocgena.serialization

//internal class PetriAtomRegistryImplSerializer(
//    private val petriAtomSerializer : SerializersModule) : KSerializer<PetriAtomRegistryImpl> {
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PetriAtomRegistryImpl") {
//        element("map", MapSerializer(PetriAtomId.serializer(), petriAtomSerializer).descriptor)
//    }
//
//    override fun serialize(encoder: Encoder, value: PetriAtomRegistryImpl) {
//        // Delegate the serialization logic to the MapSerializer
//        val mapSerializer = MapSerializer(
//            PetriAtomId.serializer(),
//            petriAtomSerializer.serializer(PetriAtom::class)
//        )
//        encoder.encodeSerializableValue(mapSerializer, value.map)
//    }
//
//    override fun deserialize(decoder: Decoder): PetriAtomRegistryImpl {
//        // Delegate the deserialization logic to the MapSerializer
//        val mapSerializer = MapSerializer(PetriAtomId.serializer(), petriAtomSerializer)
//        val map = decoder.decodeSerializableValue(mapSerializer)
//        return PetriAtomRegistryImpl(map.toMutableMap())
//    }
//}
