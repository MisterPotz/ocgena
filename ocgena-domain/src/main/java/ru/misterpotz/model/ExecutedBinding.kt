package model

import utils.html.color
import utils.html.indentLines
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

    fun prettyPrintHtmlLinesExecuted(): List<String> {
        return buildList {
            add(
                color(
                    "> executed ${finishedTransition.transition.id} [${
                        finishedTransition.timeLeftUntilFinish().print()
                    }, ${finishedTransition.duration.print()}, synchr.time: ${finishedTransition.tokenSynchronizationTime.print()}]",
                    fontColor = "rgb(164, 102, 255)",
                )
            )
            addAll( indentLines(1, color("consumed:", fontColor = "rgb(255, 49, 45)")))
            addAll(
                indentLines(
                indentation = 2,
                marginSymbol = color("- ", "rgb(255, 49, 45)"),
                item = color(
                    consumedMap.toString(),
                    fontColor = "rgb(255, 49, 45)"
                )
            )
            )
            addAll(indentLines(1, color("produced:", fontColor = "rgb(0, 133, 88)")))
            addAll(
                indentLines(
                indentation = 2,
                marginSymbol = color("+ ", "rgb(0, 133, 88)"),
                item = color(
                    producedMap.toString(),
                    fontColor = "rgb(0, 133, 88)"
                )
            )
            )
        }
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
