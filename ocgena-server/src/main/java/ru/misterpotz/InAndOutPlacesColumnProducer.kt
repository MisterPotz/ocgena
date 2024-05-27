package ru.misterpotz

class InAndOutPlacesColumnProducer(
    placeIds : List<String>,
) {
    val IN_PREFIX = "in_"
    val OUT_PREFIX = "out_"

    val inPlaces = placeIds.map {
        "$IN_PREFIX${it}"
    }
    val outPlaces = placeIds.map {
        "$OUT_PREFIX${it}"
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