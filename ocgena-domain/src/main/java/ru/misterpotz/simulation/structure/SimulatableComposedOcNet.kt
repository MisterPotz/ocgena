package ru.misterpotz.simulation.structure

import model.*
import model.time.IntervalFunction
import ru.misterpotz.model.marking.ObjectMarking
import ru.misterpotz.simulation.transition.TransitionTimesMarking

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

    interface SerializableState

    interface State {
        val tMarking: TransitionInstancesMarking
        val pMarking: ObjectMarking
        val tTimesMarking: TransitionTimesMarking
    }
}
