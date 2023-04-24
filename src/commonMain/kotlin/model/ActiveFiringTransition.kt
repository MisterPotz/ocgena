package model

import utils.print

data class ActiveFiringTransition(
    val transition: Transition,
    val lockTimeByLatestObjectToken: Time,
    val relativeTimePassedSinceLock: Time,
    val duration: Int,
    val lockedObjectTokens: ObjectMarking,
) {

    fun timeLeftUntilFinish(): Time {
        return (duration - relativeTimePassedSinceLock).coerceAtLeast(0)
    }

    fun prettyPrintState(): String {
        return """${ANSI_CYAN}ongoing ${transition.id} [${timeLeftUntilFinish().print()}, ${duration.print()}]: 
            |   $ANSI_YELLOW[locked]:
            |$ANSI_YELLOW${lockedObjectTokens.toString().prependIndent("\t")}
        """.trimMargin()
    }

    fun prettyPrintStarted() : String {
        return """${ANSI_CYAN}started ${transition.id} [${timeLeftUntilFinish().print()}, ${duration.print()}]: 
            |   $ANSI_YELLOW[locked]:
            |$ANSI_YELLOW${lockedObjectTokens.toString().prependIndent("\t")}
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
            lockTimeByLatestObjectToken == lockedObjectTokens.allTokens().maxBy { it.lastUpdateTime }.lastUpdateTime
        ) {
            "the time of start of active firing transition must be calculated by time of latest update of " +
                    "incoming tokens"
        }
    }

    companion object {
        fun create(
            transition: Transition,
            lockedObjectTokens: ObjectMarking,
            duration: Time,
        ): ActiveFiringTransition {
            val nonEmptyPlaces = lockedObjectTokens.nonEmptyPlaces()
            require(nonEmptyPlaces.isNotEmpty())
            val allTokens = lockedObjectTokens.allTokens()

            val lockTimeByLatestObjectToken = allTokens.maxBy { it.lastUpdateTime }.lastUpdateTime
            val activeFiringTransition = ActiveFiringTransition(
                transition = transition,
                lockTimeByLatestObjectToken = lockTimeByLatestObjectToken,
                relativeTimePassedSinceLock = 0,
                lockedObjectTokens = lockedObjectTokens,
                duration = duration
            )
            return activeFiringTransition.also {
                it.checkConsistency()
            }
        }
    }
}
