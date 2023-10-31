package ru.misterpotz.utils

import model.ExecutedBinding
import utils.*
import javax.inject.Inject

class ExecutedBindingDebugPrinter @Inject constructor(
    private val markingPrintingUtility: MarkingPrintingUtility
) {
    fun prettyPrintExecuted(binding: ExecutedBinding): String {
        return with(binding) {
            """${font(PURPLE_LIGHT)}> executed ${finishedActivity.transition} [${
                finishedActivity.timeLeftUntilFinish().print()
            }, ${finishedActivity.duration.print()}, synchr.time: ${finishedActivity.tokenSynchronizationTime.print()}]$ANSI_RESET
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
                """transition ${finishedActivity.transition}
            |   - consumed:
            |${prettyPrint(consumedMap).prependIndent("    ")}
            |${prettyPrint(producedMap).prependIndent("    ")}
        """.trimMargin()
            }
        }
    }
}