package model

import utils.ANSI_CYAN
import utils.ANSI_RESET
import utils.ANSI_YELLOW
import utils.font
import utils.print

data class ActiveFiringTransition(
    val transition: Transition,
    val lockTimeByLatestObjectToken: Time,
    val relativeTimePassedSinceLock: Time,
    val duration: Int,
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

    fun prettyPrintStarted() : String {
        return """${font(ANSI_CYAN)}started ${transition.id} [until exec. ${timeLeftUntilFinish().print()}, dur. ${duration.print()}]:$ANSI_RESET 
            |   ${font(ANSI_YELLOW)}[locked]:$ANSI_RESET
            |${lockedObjectTokens.toString().prependIndent("\t${font(ANSI_YELLOW)}")}
        """.trimMargin()
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
            tokenSynchronizationTime : Time,
        ): ActiveFiringTransition {
            val nonEmptyPlaces = lockedObjectTokens.nonEmptyPlaces()
            require(nonEmptyPlaces.isNotEmpty())
            val allTokens = lockedObjectTokens.allTokens()

            val lockTimeByLatestObjectToken = allTokens.maxBy { it.ownPathTime }.ownPathTime
            val activeFiringTransition = ActiveFiringTransition(
                transition = transition,
                lockTimeByLatestObjectToken = lockTimeByLatestObjectToken,
                relativeTimePassedSinceLock = 0,
                lockedObjectTokens = lockedObjectTokens,
                duration = duration,
                tokenSynchronizationTime = tokenSynchronizationTime
            )
            return activeFiringTransition.also {
                it.checkConsistency()
            }
        }
    }
}
