package ru.misterpotz.simulation.structure

import model.*
import model.time.IntervalFunction
import ru.misterpotz.model.ObjectMarking
import ru.misterpotz.simulation.transition.TransitionOccurrenceAllowedTimes

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
        val tMarking: ActiveTransitionMarking
        val pMarking: ObjectMarking
        val tTimes: TransitionOccurrenceAllowedTimes

        fun toHtmlLines() : List<String>

        fun toSerializable() : SerializableState
    }
}
