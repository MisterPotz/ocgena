package ru.misterpotz.ocgena.utils

import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType.*
import ru.misterpotz.ocgena.simulation.state.SemanticsSpecificState
import ru.misterpotz.ocgena.simulation.state.original.CurrentSimulationStateOriginal
import ru.misterpotz.ocgena.simulation.structure.State
import javax.inject.Inject

class StatePrintingUtility @Inject constructor(
    private val transitionInstanceDebugPrinter: TransitionInstanceDebugPrinter
) {
    fun toString(state: State, semanticsSpecificState: SemanticsSpecificState): String {
        return with(state) {
            """place marking:
            |${pMarking.toString().prependIndent("\t")}
            |new transitions might happen:
            |${
                when (semanticsSpecificState.type) {
                    ORIGINAL -> transitionInstanceDebugPrinter
                        .prettyPrintState(semanticsSpecificState.cast<CurrentSimulationStateOriginal>().tTimesMarking)
                        .prependIndent("\t")

                    SIMPLE_TIME_PN -> "simple time pn output not yet supported todo"
                }
            }
            |transition timed marking:
            |${
                when (semanticsSpecificState.type) {
                    ORIGINAL -> semanticsSpecificState.cast<CurrentSimulationStateOriginal>().toString()
                        .prependIndent("\t")

                    SIMPLE_TIME_PN -> "simple time pn output not yet supported todo"
                }
            }
        """.trimMargin()
        }
    }
}