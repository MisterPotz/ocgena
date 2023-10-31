package ru.misterpotz.simulation.state

import model.TransitionActivitiesMarking
import ru.misterpotz.model.marking.ObjectMarking
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.transition.TransitionOccurrenceAllowedTimes

class State() : SimulatableComposedOcNet.State {
    override val tMarking: TransitionActivitiesMarking = TransitionActivitiesMarking()
    override val pMarking: ObjectMarking = ObjectMarking()
    override val tTimes: TransitionOccurrenceAllowedTimes = TransitionOccurrenceAllowedTimes()

    override fun toString(): String {
        return """place marking:
            |${pMarking.toString().prependIndent("\t")}
            |new transitions might happen:
            |${tTimes.prettyPrintState().prependIndent("\t")}
            |transition timed marking:
            |${tMarking.toString().prependIndent("\t")}
        """.trimMargin()
    }
}
