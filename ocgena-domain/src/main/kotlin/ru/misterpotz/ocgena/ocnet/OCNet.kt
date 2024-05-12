package ru.misterpotz.ocgena.ocnet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.arcs.AalstVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArcMeta
import ru.misterpotz.ocgena.registries.*

interface OCNet {
    val ocNetType: OcNetType

    val objectTypeRegistry: ObjectTypeRegistry
    val placeTypeRegistry: PlaceTypeRegistry
    val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
    val placeRegistry: PlaceRegistry
    val transitionsRegistry: TransitionsRegistry
    val arcsRegistry: ArcsRegistry
    val petriAtomRegistry: PetriAtomRegistry

    val inputPlaces: PlaceRegistry
    val outputPlaces: PlaceRegistry
}

@Serializable
data class OCNetStruct(
    @SerialName("object_types")
    override val objectTypeRegistry: ObjectTypeRegistryMap,
    @SerialName("place_types")
    override val placeTypeRegistry: PlaceTypeRegistry,
    @SerialName("place_object_types")
    override val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    @SerialName("petri_atoms")
    override val petriAtomRegistry: PetriAtomRegistryStruct,
    @SerialName("ocNetType")
    override val ocNetType: OcNetType
) : OCNet {
    override val placeRegistry: PlaceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        PlaceRegistry(petriAtomRegistry)
    }
    override val transitionsRegistry: TransitionsRegistry by lazy(LazyThreadSafetyMode.NONE) {
        TransitionsRegistry(petriAtomRegistry)
    }
    override val arcsRegistry: ArcsRegistry by lazy(LazyThreadSafetyMode.NONE) {
        ArcsRegistry(petriAtomRegistry)
    }
    override val inputPlaces: PlaceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        placeTypeRegistry.getInputPlaces(placeRegistry)
    }
    override val outputPlaces: PlaceRegistry by lazy(LazyThreadSafetyMode.NONE) {
        placeTypeRegistry.getOutputPlaces(placeRegistry)
    }

    fun toDot(): String {
        return buildString {
            val inputPlaces = placeTypeRegistry.getInputPlaces(placeRegistry).places

            for (inputPlace in inputPlaces) {
                appendLine("${inputPlace.id} [shape=\"ellipse\", label=\"${inputPlace.id} ${placeToObjectTypeRegistry[inputPlace.id]}\"]")
            }

            appendLine("{rank=same; ${inputPlaces.joinToString("; ") { it.id }}}")

            for (place in placeRegistry.places.filter { it !in inputPlaces }) {
                appendLine("${place.id} [shape=\"ellipse\", label=\"${place.id} ${placeToObjectTypeRegistry[place.id]}\"]")
            }

            for (transition in transitionsRegistry) {
                appendLine("${transition.id} [shape=\"box\"]")
            }

            for (arc in arcsRegistry.iterable) {
                val color = when (arc.arcMeta) {
                    AalstVariableArcMeta, is LomazovaVariableArcMeta -> "color=\"black:white:black\""
                    is NormalArcMeta -> ""
                }
                val space = if (arc.arcMeta.isVar()) " " else ""
                appendLine("${arc.tailNodeId} -> ${arc.arrowNodeId} [label=\"$space${arc.arcMeta}\", $color]")
            }
        }
    }
}