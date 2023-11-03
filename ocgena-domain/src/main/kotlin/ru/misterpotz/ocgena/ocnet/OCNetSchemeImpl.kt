package ru.misterpotz.ocgena.ocnet

import model.ArcsRegistry
import ru.misterpotz.ocgena.registries.PlaceRegistry
import ru.misterpotz.ocgena.registries.TransitionsRegistry
import ru.misterpotz.ocgena.eventlog.ObjectTypes
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc


//class OCNetSchemeImpl(
//    override val placeRegistry : PlaceRegistry,
//    override val transitionsRegistry: TransitionsRegistry,
//    override val arcsRegistry : ArcsRegistry,
//    override val objectTypes: ObjectTypes,
//    override val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
//    val allArcs: List<Arc>,
//): OCNetScheme {
//
//    override fun toString(): String {
//        return """
//            |Output(
//            |   places: $placeRegistry
//            |   transitions: $transitionsRegistry,
//            |   arcs: $arcsRegistry
//            |)
//        """.trimMargin()
//    }
//}
//
