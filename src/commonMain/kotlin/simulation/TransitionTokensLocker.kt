package simulation

import model.ActiveFiringTransition
import model.time.IntervalFunction
import model.ActiveTransitionMarking
import simulation.binding.EnabledBindingWithTokens
import simulation.time.NextTransitionOccurenceAllowedTimeSelector
import simulation.time.TransitionOccurrenceAllowedTimes
import kotlin.random.Random

class TransitionTokensLocker(
    private val pMarkingProvider: PMarkingProvider,
    private val tMarking: ActiveTransitionMarking,
    private val nextTransitionOccurenceAllowedTimeSelector: NextTransitionOccurenceAllowedTimeSelector,
    private val tTimes : TransitionOccurrenceAllowedTimes,
    private val intervalFunction: IntervalFunction,
    private val logger: Logger
) {
    private fun recordActiveTransition(enabledBindingWithTokens: EnabledBindingWithTokens) {
        val transition = enabledBindingWithTokens.transition
        val interval = intervalFunction[transition]

        val randomSelectedDuration = Random.nextInt(
            from = interval.earlyFiringTime,
            until = interval.latestFiringTime + 1
        )

        val tMarkingValue = ActiveFiringTransition.create(
            transition = transition,
            lockedObjectTokens = enabledBindingWithTokens.involvedObjectTokens,
            duration = randomSelectedDuration,
            tokenSynchronizationTime = enabledBindingWithTokens.synchronizationTime
        )

        tMarking.pushTMarking(tMarkingValue)
        val newNextAllowedTime = nextTransitionOccurenceAllowedTimeSelector.getNewNextOccurrenceTime(transition)

        tTimes.setNextAllowedTime(
            transition = transition,
            time = newNextAllowedTime
        )
        logger.onTransitionStart(transition = tMarkingValue)
    }

    private fun lockTokensInPMarking(enabledBindingWithTokens: EnabledBindingWithTokens) {
        pMarkingProvider.pMarking -= enabledBindingWithTokens.involvedObjectTokens
    }

    fun lockTokensAndRecordActiveTransition(enabledBindingWithTokens: EnabledBindingWithTokens) {
        lockTokensInPMarking(enabledBindingWithTokens)
        recordActiveTransition(enabledBindingWithTokens)
    }
}
