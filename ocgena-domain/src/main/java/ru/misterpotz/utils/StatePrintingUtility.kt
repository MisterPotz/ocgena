package ru.misterpotz.utils

import ru.misterpotz.simulation.state.State
import javax.inject.Inject

class StatePrintingUtility @Inject constructor(
    private val transitionInstanceDebugPrinter: TransitionInstanceDebugPrinter
) {
    fun toString(state: State): String {
        return with(state) {
            """place marking:
            |${pMarking.toString().prependIndent("\t")}
            |new transitions might happen:
            |${transitionInstanceDebugPrinter.prettyPrintState(tTimesMarking).prependIndent("\t")}
            |transition timed marking:
            |${tMarking.toString().prependIndent("\t")}
        """.trimMargin()
        }
    }
}