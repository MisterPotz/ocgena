package model


class TMarking {
    private val transitionsToTMarkingValue = mutableMapOf<Transition, ActiveFiringTransitions>()
    operator fun get(transition: model.Transition): ActiveFiringTransitions? {
        return transitionsToTMarkingValue[transition]
    }

    fun shiftByTime(time : Time) {
        transitionsToTMarkingValue.forEach { entry ->
            entry.value.shiftByTime(time)
        }
    }

    fun getActiveTransitionWithEarliestFinish() : ActiveFiringTransition {
        return transitionsToTMarkingValue.minBy { entry ->
            val activeFiringTransitions = entry.value
            val timeLeftUntilFinish = activeFiringTransitions
                .getWithEarliestFinishTime()
                .timeLeftUntilFinish()

            return@minBy timeLeftUntilFinish
        }.value.getWithEarliestFinishTime()
    }

    fun getEndedTransitions() : Collection<ActiveFiringTransition> {
        return transitionsToTMarkingValue.values.fold(mutableListOf<ActiveFiringTransition>()) { accum, transitions ->
            val endedTransitions = transitions.getEndedTransitions()
            accum.addAll(endedTransitions)
            return@fold accum
        }
    }

    fun pushTMarking(tMarkingValue: ActiveFiringTransition) {
        val transition = tMarkingValue.transition
        val current = transitionsToTMarkingValue.getOrElse(transition) {
            ActiveFiringTransitions()
        }
        current.add(tMarkingValue)
    }
}

data class ActiveFiringTransition(
    val transition: Transition,
    val lockTimeByLatestObjectToken: Time,
    val relativeTimePassedSinceLock: Time,
    val duration: Int,
    val lockedObjectTokens: ObjectMarking,
) {

    fun timeLeftUntilFinish() : Time {
        return (duration - relativeTimePassedSinceLock).coerceAtLeast(0)
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
            duration : Time
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
