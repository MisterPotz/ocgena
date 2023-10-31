package ru.misterpotz.simulation.binding

import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.Time
import utils.*

data class ExecutedBinding(
    val finishedTransitionInstance: TransitionInstance,
    val finishedTime: Time,
    val consumedMap: ImmutableObjectMarking,
    val producedMap: ImmutableObjectMarking,
) {

    override fun toString(): String {
        return """$ANSI_PURPLE> executed ${finishedTransitionInstance.transition}
            |${"\t$ANSI_RED"}- consumed:
            |${ANSI_RED}${consumedMap.toString().prependIndent("\t- ")}
            |$ANSI_GREEN${"\t"}+ produced:
            |$ANSI_GREEN${producedMap.toString().prependIndent("\t+ ")}
        """.trimMargin()
    }
}
