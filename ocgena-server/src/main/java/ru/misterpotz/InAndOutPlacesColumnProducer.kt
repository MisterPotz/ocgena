package ru.misterpotz

import ru.misterpotz.ocgena.ocnet.OCNetStruct
import javax.inject.Inject

class InAndOutPlacesColumnProducer @Inject constructor(
    ocNetStruct: OCNetStruct
) {
    val IN_PREFIX = "in_"
    val OUT_PREFIX = "out_"

    val inPlaces = ocNetStruct.placeRegistry.places.map {
        "$IN_PREFIX${it.id}"
    }
    val outPlaces = ocNetStruct.placeRegistry.places.map {
        "$OUT_PREFIX${it.id}"
    }

    val merged = inPlaces.toMutableList().apply {
        addAll(outPlaces)
    }

    fun inNameTransformer(inPlace: String): String {
        return "$IN_PREFIX$inPlace"
    }

    fun inNameDetransformer(inPlace: String) : String {
        return inPlace.removePrefix(IN_PREFIX)
    }

    fun outNameTransformer(outPlace: String): String {
        return "$OUT_PREFIX$outPlace"
    }

    fun outNameDetransformer(outPlace: String) : String {
        return outPlace.removePrefix(OUT_PREFIX)
    }
}