package ru.misterpotz.ocgena.simulation.structure

import ru.misterpotz.ocgena.simulation.config.IntervalFunction
import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionToInstancesMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionToTimeUntilInstanceAllowedMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.PetriAtomRegistry

interface OcNetInstance {
    val ocNet: OCNet
    val intervalFunction: IntervalFunction
    val ocNetType: OcNetType

    fun createInitialState(): State
    
    interface State {
        val tMarking: TransitionToInstancesMarking
        val pMarking: PlaceToObjectMarking
        val tTimesMarking: TransitionToTimeUntilInstanceAllowedMarking
        val petriAtomRegistry : PetriAtomRegistry
    }
}
