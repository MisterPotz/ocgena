package model

import utils.print

const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "\u001B[30m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001B[32m"
const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_BLUE = "\u001B[34m"
const val ANSI_PURPLE = "\u001B[35m"
const val ANSI_CYAN = "\u001B[36m"
const val ANSI_WHITE = "\u001B[37m"

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
        }, ${finishedTransition.duration.print()}]
            |${"\t$ANSI_RED"}- consumed:
            |${ANSI_RED}${consumedMap.toString().prependIndent("\t- ")}
            |$ANSI_GREEN${"\t"}+ produced:
            |$ANSI_GREEN${producedMap.toString().prependIndent("\t+ ")}
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
