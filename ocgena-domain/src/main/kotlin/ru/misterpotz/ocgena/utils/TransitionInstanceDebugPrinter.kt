package ru.misterpotz.ocgena.utils

import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.collections.TransitionInstancesList
import ru.misterpotz.ocgena.registries.original.TransitionToTimeUntilInstanceAllowedRegistryOriginal
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

    fun prettyPrintState(transitionTimesMarking: TransitionToTimeUntilInstanceAllowedRegistryOriginal): String {
        return with(transitionTimesMarking) {
            keys.joinToString(separator = "\n") {
                """${it} permitted in ${getNextAllowedTime(it)?.print()}"""
            }
        }
    }

    fun prettyPrint(transitionInstancesList: TransitionInstancesList): String {
        return with(transitionInstancesList) {
            iterable().toList().joinToString(separator = "\n") { prettyPrintState(it) }
        }
    }
}
