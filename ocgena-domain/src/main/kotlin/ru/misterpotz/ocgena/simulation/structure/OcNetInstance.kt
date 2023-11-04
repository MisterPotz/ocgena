package ru.misterpotz.ocgena.simulation.structure

import model.ArcsRegistry
import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionToInstancesMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionToTimeUntilInstanceAllowedMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation.config.IntervalFunction

interface OcNetInstance : OCNet {
    val ocNet: OCNet
    val intervalFunction: IntervalFunction
    val ocNetType: OcNetType

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

    fun createInitialState(): State
    
    interface State {
        val tMarking: TransitionToInstancesMarking
        val pMarking: PlaceToObjectMarking
        val tTimesMarking: TransitionToTimeUntilInstanceAllowedMarking
    }
}
