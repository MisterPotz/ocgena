package ru.misterpotz.ocgena.simulation.binding

import ru.misterpotz.ocgena.collections.transitions.TransitionInstance
import ru.misterpotz.ocgena.simulation.Time
import utils.*

data class ExecutedBinding(
    val finishedTransitionInstance: TransitionInstance,
    val finishedTime: Time,
    val consumedMap: ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking,
    val producedMap: ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking,
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
