package model

import eventlog.Timestamp
import utils.*


data class ExecutedBinding(
    val finishedTransition: ActiveFiringTransition,
    val finishedTime: Int,
    val consumedMap: ImmutableObjectMarking,
    val producedMap: ImmutableObjectMarking,
) {
    fun prettyString(): String {
        return """transition ${finishedTransition.transition}
            |   - consumed:
            |${consumedMap.prettyPrint().prependIndent("    ")}
            |${producedMap.prettyPrint().prependIndent("    ")}
        """.trimMargin()
    }

    fun prettyPrintExecuted(): String {
        return """${font(PURPLE_LIGHT)}> executed ${finishedTransition.transition.id} [${
            finishedTransition.timeLeftUntilFinish().print()
        }, ${finishedTransition.duration.print()}, synchr.time: ${finishedTransition.tokenSynchronizationTime.print()}]$ANSI_RESET
            |${"\t${font(ANSI_RED)}"}consumed:$ANSI_RESET
            |${consumedMap.toString().prependIndent("${font(ANSI_RED)}\t- ")}
            |${"\t"}${font(ANSI_GREEN)}produced:$ANSI_RESET
            |${producedMap.toString().prependIndent("${font(ANSI_GREEN)}\t+ ")}
        """.trimMargin()
    }

    override fun toString(): String {
        return """$ANSI_PURPLE> executed ${finishedTransition.transition.id}
            |${"\t$ANSI_RED"}- consumed:
            |${ANSI_RED}${consumedMap.toString().prependIndent("\t- ")}
            |$ANSI_GREEN${"\t"}+ produced:
            |$ANSI_GREEN${producedMap.toString().prependIndent("\t+ ")}
        """.trimMargin()
    }
}
