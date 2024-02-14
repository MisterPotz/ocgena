package ru.misterpotz

import ru.misterpotz.di.ServerSimulationConfig
import javax.inject.Inject

class InAndOutPlacesColumnProducer @Inject constructor(private val serverSimulationConfig: ServerSimulationConfig) {
    val IN_PREFIX = "in_"
    val OUT_PREFIX = "out_"

    val inPlaces = serverSimulationConfig.simulationConfig.ocNet.placeRegistry.places.map {
        "$IN_PREFIX${it.id}"
    }
    val outPlaces = serverSimulationConfig.simulationConfig.ocNet.placeRegistry.places.map {
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