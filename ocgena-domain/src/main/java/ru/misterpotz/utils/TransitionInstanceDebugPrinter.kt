package ru.misterpotz.utils

import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.marking.transitions.TransitionInstancesList
import utils.*
import javax.inject.Inject

class TransitionInstanceDebugPrinter @Inject constructor(
    private val markingPrintingUtility: MarkingPrintingUtility
) {

    fun prettyPrintStarted(transitionInstance: TransitionInstance): String {
        return with(transitionInstance) {
            """${font(ANSI_CYAN)}started $transition [until exec. ${timeLeftUntilFinish().print()}, dur. ${duration.print()}]:$ANSI_RESET 
            |   ${font(ANSI_YELLOW)}[locked]:$ANSI_RESET
            |${lockedObjectTokens.toString().prependIndent("\t${font(ANSI_YELLOW)}")}
        """.trimMargin()
        }
    }

    fun prettyPrintState(transitionInstance: TransitionInstance): String {
        return with(transitionInstance) {
            """${font(ANSI_CYAN)}ongoing $transition [until exec. ${timeLeftUntilFinish().print()}, dur. ${duration.print()}]: 
            |   ${font(ANSI_YELLOW)}[locked]:$ANSI_RESET
            |${lockedObjectTokens.toString().prependIndent("\t${font(ANSI_YELLOW)}")}
        """.trimMargin()
        }
    }

    fun prettyPrint(transitionInstancesList: TransitionInstancesList): String {
        return with(transitionInstancesList) {
            iterable().toList().joinToString(separator = "\n") { prettyPrintState(it) }
        }
    }
}
