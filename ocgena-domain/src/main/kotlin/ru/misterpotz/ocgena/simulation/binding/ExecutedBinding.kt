package ru.misterpotz.ocgena.simulation.binding

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.utils.ANSI_GREEN
import ru.misterpotz.ocgena.utils.ANSI_PURPLE
import ru.misterpotz.ocgena.utils.ANSI_RED

data class ExecutedBinding(
    val finishedTransitionInstance: TransitionInstance,
    val finishedTime: Time,
    val consumedMap: ImmutablePlaceToObjectMarking,
    val producedMap: ImmutablePlaceToObjectMarking,
) {

    override fun toString(): String {
        return """$ANSI_PURPLE> executed ${finishedTransitionInstance.transition}
            |${"\t$ANSI_RED"}- consumed:
            |$ANSI_RED${consumedMap.toString().prependIndent("\t- ")}
            |$ANSI_GREEN${"\t"}+ produced:
            |$ANSI_GREEN${producedMap.toString().prependIndent("\t+ ")}
        """.trimMargin()
    }
}
