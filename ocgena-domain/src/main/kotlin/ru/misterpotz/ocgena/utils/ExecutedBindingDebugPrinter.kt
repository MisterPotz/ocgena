package ru.misterpotz.ocgena.utils

import ru.misterpotz.ocgena.simulation.binding.ExecutedBinding
import utils.*
import javax.inject.Inject

class ExecutedBindingDebugPrinter @Inject constructor(
    private val markingPrintingUtility: MarkingPrintingUtility
) {
    fun prettyPrintExecuted(binding: ExecutedBinding): String {
        return with(binding) {
            """${font(PURPLE_LIGHT)}> executed ${finishedTransitionInstance.transition} [${
                finishedTransitionInstance.timeLeftUntilFinish().print()
            }, ${finishedTransitionInstance.duration.print()}, synchr.time: ${finishedTransitionInstance.tokenSynchronizationTime.print()}]$ANSI_RESET
            |${"\t${font(ANSI_RED)}"}consumed:$ANSI_RESET
            |${consumedMap.toString().prependIndent("${font(ANSI_RED)}\t- ")}
            |${"\t"}${font(ANSI_GREEN)}produced:$ANSI_RESET
            |${producedMap.toString().prependIndent("${font(ANSI_GREEN)}\t+ ")}
        """.trimMargin()
        }
    }

    fun prettyString(binding: ExecutedBinding): String {
        return with(binding) {
            with(markingPrintingUtility) {
                """transition ${finishedTransitionInstance.transition}
            |   - consumed:
            |${prettyPrint(consumedMap).prependIndent("    ")}
            |${prettyPrint(producedMap).prependIndent("    ")}
        """.trimMargin()
            }
        }
    }
}