package ru.misterpotz.ocgena.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.registries.ObjectTypeRegistryMap
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry


//object OCNetSerializer : KSerializer<OCNet> {
//    val ocNetStructSerializer = OCNetStruct.serializer()
//    val objectTypeRegistrySerializer = ObjectTypeRegistryMap.serializer()
//    val placeTypeRegistry = ObjectTypeRegistryMap.serializer()
//    val placeToObjectTypeRegistry = PlaceToObjectTypeRegistry.serializer()
//
//    //    val petriAtomRegistrySerializer = PetriAtomRegistryImplSerializer(
////        petriAtomSerializer = serializersModule.getContextual())
//    override val descriptor: SerialDescriptor = ocNetStructSerializer.descriptor
//
//    override fun deserialize(decoder: Decoder): OCNet {
//        ocNetStructSerializer.deserialize(decoder)
//    }
//
//    override fun serialize(encoder: Encoder, value: OCNet) {
//        ocNetStructSerializer.serialize(encoder, value = value as OCNetStruct)
//    }
//}
