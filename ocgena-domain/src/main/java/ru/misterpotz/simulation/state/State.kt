package ru.misterpotz.simulation.state

import model.TransitionInstancesMarking
import ru.misterpotz.model.marking.ObjectMarking
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.transition.TransitionOccurrenceAllowedTimes
import ru.misterpotz.simulation.transition.TransitionTimesMarking

class State() : SimulatableComposedOcNet.State {
    override val tMarking: TransitionInstancesMarking = TransitionInstancesMarking()
    override val pMarking: ObjectMarking = ObjectMarking()
    override val tTimesMarking: TransitionTimesMarking = TransitionOccurrenceAllowedTimes()

    override fun toString(): String {
        return """place marking:
            |${pMarking.toString().prependIndent("\t")}
            |new transitions might happen:
            |${tTimesMarking.prettyPrintState().prependIndent("\t")}
            |transition timed marking:
            |${tMarking.toString().prependIndent("\t")}
        """.trimMargin()
    }
}
