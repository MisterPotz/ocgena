package model

import kotlinx.serialization.Serializable
import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.Time
import utils.html.color
import utils.html.indentLines
import utils.ANSI_CYAN
import utils.ANSI_RESET
import utils.ANSI_YELLOW
import utils.font
import utils.print

@Serializable
data class OngoingActivity(
    val transition: TransitionId,
    val lockTimeByLatestObjectToken: Time,
    val relativeTimePassedSinceLock: Time,
    val duration: Int,
    val startedAt: Time,
    val tokenSynchronizationTime: Time,
    val lockedObjectTokens: ImmutableObjectMarking,
) {
    fun timeLeftUntilFinish(): Time {
        return (duration - relativeTimePassedSinceLock).coerceAtLeast(0)
    }

    fun prettyPrintState(): String {
        return """${font(ANSI_CYAN)}ongoing ${transition.id} [until exec. ${timeLeftUntilFinish().print()}, dur. ${duration.print()}]: 
            |   ${font(ANSI_YELLOW)}[locked]:$ANSI_RESET
            |${lockedObjectTokens.toString().prependIndent("\t${font(ANSI_YELLOW)}")}
        """.trimMargin()
    }

    fun prettyPrintHtmlLinesState(): List<String> {
        return buildList {
            add(
                color(
                    "ongoing ${transition.id} [until exec. ${timeLeftUntilFinish().print()}, dur. ${duration.print()}]:",
                    fontColor = "rgb(0, 133, 88)",
                )
            )
            addAll(indentLines(indentation = 1, color("[locked]:", fontColor = "rgb(209, 194, 0)")))
            addAll(

                indentLines(
                    indentation = 2,
                    color(
                        lockedObjectTokens.htmlLines(),
                        fontColor = "rgb(209, 194, 0)"
                    ),
                    marginSymbol = "~",
                )
            )
        }
    }

    fun prettyPrintStarted(): String {
        return """${font(ANSI_CYAN)}started ${transition.id} [until exec. ${timeLeftUntilFinish().print()}, dur. ${duration.print()}]:$ANSI_RESET 
            |   ${font(ANSI_YELLOW)}[locked]:$ANSI_RESET
            |${lockedObjectTokens.toString().prependIndent("\t${font(ANSI_YELLOW)}")}
        """.trimMargin()
    }

    fun prettyPrintHtmlLinesStarted(): List<String> {
        return buildList {
            add(
                color(
                    "started ${transition.id} [until exec. ${timeLeftUntilFinish().print()}, dur. ${duration.print()}]:",
                    fontColor = "rgb(0, 133, 88)",
                )
            )
            addAll(indentLines(indentation = 1, color("[locked]:", fontColor = "rgb(209, 194, 0)")))
            addAll(
                indentLines(
                    indentation = 2,
                    color(
                        lockedObjectTokens.htmlLines(),
                        fontColor = "rgb(209, 194, 0)"
                    ),
                    marginSymbol = "~",
                )
            )
        }
    }

    override fun toString(): String {

        return """$ANSI_CYAN> started ${transition.id} : 
            |   $ANSI_YELLOW[locked]:
            |$ANSI_YELLOW${lockedObjectTokens.toString().prependIndent("\t")}
        """.trimMargin()
    }

    fun checkConsistency() {
        require(
            lockTimeByLatestObjectToken == lockedObjectTokens.allTokens().maxBy { it.ownPathTime }.ownPathTime
        ) {
            "the time of start of active firing transition must be calculated by time of latest update of " +
                    "incoming tokens"
        }
    }

    companion object {
        fun create(
            transition: Transition,
            lockedObjectTokens: ImmutableObjectMarking,
            duration: Time,
            startedAt: Time,
            tokenSynchronizationTime: Time,
        ): OngoingActivity {
            val nonEmptyPlaces = lockedObjectTokens.nonEmptyPlaces()
            require(nonEmptyPlaces.isNotEmpty())
            val allTokens = lockedObjectTokens.allTokens()

            val lockTimeByLatestObjectToken = allTokens.maxBy { it.ownPathTime }.ownPathTime
            val activeFiringTransition = OngoingActivity(
                transition = transition,
                lockTimeByLatestObjectToken = lockTimeByLatestObjectToken,
                relativeTimePassedSinceLock = 0,
                lockedObjectTokens = lockedObjectTokens,
                duration = duration,
                startedAt = startedAt,
                tokenSynchronizationTime = tokenSynchronizationTime
            )
            return activeFiringTransition.also {
                it.checkConsistency()
            }
        }
    }
}
