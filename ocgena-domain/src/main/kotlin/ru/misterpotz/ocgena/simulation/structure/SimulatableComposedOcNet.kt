package ru.misterpotz.ocgena.simulation.structure

import model.StaticCoreOcNet
import model.time.IntervalFunction
import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionInstancesMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionTimesMarking
import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.PetriAtomRegistry

interface SimulatableComposedOcNet<T : SimulatableComposedOcNet<T>> {
    val coreOcNet: StaticCoreOcNet
    val arcMultiplicity: ArcMultiplicity
    val intervalFunction: IntervalFunction
    val ocNetType: OcNetType

    fun createInitialState(): State

    fun fullCopy() : T
    
    interface State {
        val tMarking: TransitionInstancesMarking
        val pMarking: PlaceToObjectMarking
        val tTimesMarking: TransitionTimesMarking
        val petriAtomRegistry : PetriAtomRegistry
    }
}
