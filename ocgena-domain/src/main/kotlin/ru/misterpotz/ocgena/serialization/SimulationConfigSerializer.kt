//package ru.misterpotz.ocgena.serialization
//
//import kotlinx.serialization.ExperimentalSerializationApi
//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.SerializationException
//import kotlinx.serialization.builtins.serializer
//import kotlinx.serialization.descriptors.SerialDescriptor
//import kotlinx.serialization.descriptors.buildClassSerialDescriptor
//import kotlinx.serialization.descriptors.element
//import kotlinx.serialization.encoding.CompositeDecoder
//import kotlinx.serialization.encoding.Decoder
//import kotlinx.serialization.encoding.Encoder
//import kotlinx.serialization.modules.SerializersModule
//import ru.misterpotz.ocgena.ocnet.OCNet
//import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
//import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
//import ru.misterpotz.ocgena.simulation.config.MarkingScheme
//import ru.misterpotz.ocgena.simulation.config.SimulationConfig
//import ru.misterpotz.ocgena.simulation.config.TokenGenerationConfig
//import ru.misterpotz.ocgena.simulation.config.original.TransitionInstancesTimesSpec
//
//
//
//// Assuming you have defined the OCNetSerializer somewhere else
//
//// Custom serializer for SimulationConfig
//class SimulationConfigSerializer(
//    private val serializationModule : SerializersModule,
//    private val ocNetSerializer: KSerializer<OCNet>
//) : KSerializer<SimulationConfig> {
//    val ocNet = serializationModule.
//
//    override val descriptor: SerialDescriptor =
//        buildClassSerialDescriptor("SimulationConfig") {
//            element("ocNet", ocNetSerializer.descriptor)
//            element("initialMarking", MarkingScheme.serializer().descriptor)
//            element("transitionInstancesTimesSpec", TransitionInstancesTimesSpec.serializer().descriptor)
//            element<Int?>("randomSeed", isOptional = true)
//            element("nodeToLabelRegistry", NodeToLabelRegistry.serializer().descriptor)
//            element<TokenGenerationConfig?>("tokenGeneration", isOptional = true)
//            element("ocNetType", OcNetType.serializer().descriptor)
//        }
//
//    @OptIn(ExperimentalSerializationApi::class)
//    override fun serialize(encoder: Encoder, value: SimulationConfig) {
//        val compositeEncoder = encoder.beginStructure(descriptor)
//        compositeEncoder.encodeSerializableElement(descriptor, 0, ocNetSerializer, value.ocNet)
//        compositeEncoder.encodeSerializableElement(descriptor, 1, MarkingScheme.serializer(), value.initialMarking)
//        compositeEncoder.encodeSerializableElement(
//            descriptor,
//            2,
//            TransitionInstancesTimesSpec.serializer(),
//            value.transitionInstancesTimesSpec
//        )
//        if (value.randomSeed != null) {
//            compositeEncoder.encodeNullableSerializableElement(descriptor, 3, Int.serializer(), value.randomSeed)
//        }
//        compositeEncoder.encodeSerializableElement(
//            descriptor,
//            4,
//            NodeToLabelRegistry.serializer(),
//            value.nodeToLabelRegistry
//        )
//        if (value.tokenGeneration != null) {
//            compositeEncoder.encodeNullableSerializableElement(
//                descriptor,
//                5,
//                TokenGenerationConfig.serializer(),
//                value.tokenGeneration
//            )
//        }
//        compositeEncoder.encodeSerializableElement(descriptor, 6, OcNetType.serializer(), value.ocNetType)
//        compositeEncoder.endStructure(descriptor)
//    }
//
//    @OptIn(ExperimentalSerializationApi::class)
//    override fun deserialize(decoder: Decoder): SimulationConfig {
//        val dec = decoder.beginStructure(descriptor)
//        var ocNet: OCNet? = null
//        var initialMarking: MarkingScheme? = null
//        var transitionInstancesTimesSpec: TransitionInstancesTimesSpec? = null
//        var randomSeed: Int? = null
//        var nodeToLabelRegistry: NodeToLabelRegistry? = null
//        var tokenGeneration: TokenGenerationConfig? = null
//        var ocNetType: OcNetType? = null
//
//        loop@ while (true) {
//            when (val index = dec.decodeElementIndex(descriptor)) {
//                CompositeDecoder.DECODE_DONE -> break@loop
//                0 -> ocNet = dec.decodeSerializableElement(descriptor, index, ocNetSerializer)
//                1 -> initialMarking = dec.decodeSerializableElement(descriptor, index, MarkingScheme.serializer())
//                2 -> transitionInstancesTimesSpec =
//                    dec.decodeSerializableElement(descriptor, index, TransitionInstancesTimesSpec.serializer())
//
//                3 -> randomSeed = dec.decodeNullableSerializableElement(descriptor, index, Int.serializer())
//                4 -> nodeToLabelRegistry =
//                    dec.decodeSerializableElement(descriptor, index, NodeToLabelRegistry.serializer())
//
//                5 -> tokenGeneration =
//                    dec.decodeNullableSerializableElement(descriptor, index, TokenGenerationConfig.serializer())
//
//                6 -> ocNetType = dec.decodeSerializableElement(descriptor, index, OcNetType.serializer())
//                else -> throw SerializationException("Unexpected index: $index")
//            }
//        }
//        dec.endStructure(descriptor)
//
//        return SimulationConfig(
//            ocNet ?: throw SerializationException("Missing ocNet"),
//            initialMarking ?: throw SerializationException("Missing initialMarking"),
//            transitionInstancesTimesSpec ?: throw SerializationException("Missing transitionInstancesTimesSpec"),
//            randomSeed,
//            nodeToLabelRegistry ?: NodeToLabelRegistry(),
//            tokenGeneration,
//            ocNetType ?: throw SerializationException("Missing ocNetType")
//        )
//    }
//}