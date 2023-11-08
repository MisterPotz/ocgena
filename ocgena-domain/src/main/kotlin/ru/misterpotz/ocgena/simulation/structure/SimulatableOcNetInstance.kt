package ru.misterpotz.ocgena.simulation.structure

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.registries.TransitionToInstancesRegistry
import ru.misterpotz.ocgena.registries.TransitionToTimeUntilInstanceAllowedRegistry
import ru.misterpotz.ocgena.registries.ArcsRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation.config.TransitionInstancesTimesSpec

interface SimulatableOcNetInstance : OCNet {
    val ocNet: OCNet
    val ocNetType: OcNetType
    val state : State

    override val objectTypeRegistry: ObjectTypeRegistry
        get() = ocNet.objectTypeRegistry
    override val placeTypeRegistry: PlaceTypeRegistry
        get() = ocNet.placeTypeRegistry
    override val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
        get() = ocNet.placeToObjectTypeRegistry
    override val placeRegistry: PlaceRegistry
        get() = ocNet.placeRegistry
    override val transitionsRegistry: TransitionsRegistry
        get() = ocNet.transitionsRegistry
    override val arcsRegistry: ArcsRegistry
        get() = ocNet.arcsRegistry
    override val petriAtomRegistry: PetriAtomRegistry
        get() = ocNet.petriAtomRegistry
    override val inputPlaces: PlaceRegistry
        get() = ocNet.inputPlaces
    override val outputPlaces: PlaceRegistry
        get() = ocNet.outputPlaces

}

interface State {
    val tMarking: TransitionToInstancesRegistry
    val pMarking: PlaceToObjectMarking
    val tTimesMarking: TransitionToTimeUntilInstanceAllowedRegistry
    val arcsMultiplicityRegistry : ArcsMultiplicityRegistry
    val transitionToPlacesByObjectTypeIndexRegistry : TransitionToPlacesByObjectTypeIndexRegistry
}