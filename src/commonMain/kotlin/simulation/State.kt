package simulation

import model.ActiveTransitionMarking
import model.ObjectMarking
import simulation.time.TransitionOccurrenceAllowedTimes

class State() : SimulatableComposedOcNet.State {
    override val tMarking: ActiveTransitionMarking = ActiveTransitionMarking()
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
