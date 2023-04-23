package simulation

import model.ArcMultiplicity
import model.IntervalFunction
import model.ObjectMarking
import model.StaticCoreOcNet
import model.TMarking

interface SimulatableComposedOcNet<T : SimulatableComposedOcNet<T>> {
    val coreOcNet: StaticCoreOcNet
    val arcMultiplicity: ArcMultiplicity
    val intervalFunction: IntervalFunction
    fun createInitialState(): State

    fun initialize() {
        coreOcNet.places.reindexArcs()
    }

    fun fullCopy() : T

    interface State {
        val tMarking: TMarking
        val pMarking: ObjectMarking
    }
}
