package ru.misterpotz.simulation.structure

import model.*
import model.time.IntervalFunction
import ru.misterpotz.marking.objects.ObjectMarking
import ru.misterpotz.marking.transitions.TransitionInstancesMarking
import ru.misterpotz.marking.transitions.TransitionTimesMarking

interface SimulatableComposedOcNet<T : SimulatableComposedOcNet<T>> {
    val coreOcNet: StaticCoreOcNet
    val arcMultiplicity: ArcMultiplicity
    val intervalFunction: IntervalFunction
    val ocNetType: OcNetType

    fun createInitialState(): State

    fun initialize() {
        coreOcNet.places.reindexArcs()
        coreOcNet.transitions.reindexArcs()
    }

    fun fullCopy() : T
    
    interface State {
        val tMarking: TransitionInstancesMarking
        val pMarking: ObjectMarking
        val tTimesMarking: TransitionTimesMarking
    }
}
