package model

import utils.ANSI_GREEN
import utils.ANSI_PURPLE
import utils.ANSI_RED
import utils.print



data class ExecutedBinding(
    val finishedTransition: ActiveFiringTransition,
    val consumedMap: ObjectMarking,
    val producedMap: ObjectMarking,
) {
    fun prettyString(): String {
        return """transition ${finishedTransition.transition}
            |   - consumed:
            |${consumedMap.prettyPrint().prependIndent("    ")}
            |${producedMap.prettyPrint().prependIndent("    ")}
        """.trimMargin()
    }

    fun prettyPrintExecuted(): String {
        return """$ANSI_PURPLE> executed ${finishedTransition.transition.id} [${
            finishedTransition.timeLeftUntilFinish().print()
        }, ${finishedTransition.duration.print()}, synchr.time: ${finishedTransition.tokenSynchronizationTime.print()}]
            |${"\t$ANSI_RED"}- consumed:
            |${ANSI_RED}${consumedMap.toString().prependIndent("\t- ${ANSI_RED}")}
            |$ANSI_GREEN${"\t"}+ produced:
            |$ANSI_GREEN${producedMap.toString().prependIndent("\t+ $ANSI_GREEN")}
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
