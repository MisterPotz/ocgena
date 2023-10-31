package ru.misterpotz.simulation.state

import model.ActiveTransitionMarking
import ru.misterpotz.model.marking.ObjectMarking
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.transition.TransitionOccurrenceAllowedTimes
import utils.html.indentLines
import utils.html.underline

class State() : SimulatableComposedOcNet.State {
    override val tMarking: ActiveTransitionMarking = ActiveTransitionMarking()
    override val pMarking: ObjectMarking = ObjectMarking()
    override val tTimes: TransitionOccurrenceAllowedTimes = TransitionOccurrenceAllowedTimes()

    override fun toHtmlLines(): List<String> {
        return buildList {
            add(underline("place marking:"))
            addAll(indentLines(1, pMarking.htmlLines()))
            add(underline("new transitions might happen:"))
            addAll(indentLines(1, tTimes.htmlLines()))
            add(underline("transition timed marking:"))
            addAll(indentLines(1, tMarking.htmlLinesState()))
        }
    }

    override fun toSerializable() : SerializableState {
        return SerializableState(
            tMarking = tMarking.toSerializable(),
            pMarking = pMarking.toImmutable(),
            tMinTimes = tTimes.toSerializable(),
        )
    }

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
