package simulation

import model.ArcMultiplicity
import model.time.IntervalFunction
import model.ObjectMarking
import model.StaticCoreOcNet
import model.ActiveTransitionMarking
import simulation.time.TransitionOccurrenceAllowedTimes

interface SimulatableComposedOcNet<T : SimulatableComposedOcNet<T>> {
    val coreOcNet: StaticCoreOcNet
    val arcMultiplicity: ArcMultiplicity
    val intervalFunction: IntervalFunction
    fun createInitialState(): State

    fun initialize() {
        coreOcNet.places.reindexArcs()
        coreOcNet.transitions.reindexArcs()
    }

    fun fullCopy() : T

    interface State {
        val tMarking: ActiveTransitionMarking
        val pMarking: ObjectMarking
        val tTimes: TransitionOccurrenceAllowedTimes
    }
}
