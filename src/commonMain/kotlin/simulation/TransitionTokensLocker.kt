package simulation

import model.ActiveFiringTransition
import model.IntervalFunction
import model.TMarking
import simulation.binding.EnabledBindingWithTokens
import kotlin.random.Random

class TransitionTokensLocker(
    private val pMarkingProvider: PMarkingProvider,
    private val tMarking: TMarking,
    private val intervalFunction: IntervalFunction,
    private val logger: Logger
) {
    private fun recordActiveTransition(enabledBindingWithTokens: EnabledBindingWithTokens) {
        val interval = intervalFunction[enabledBindingWithTokens.transition]

        val randomSelectedDuration = Random.nextInt(
            from = interval.earlyFiringTime,
            until = interval.latestFiringTime + 1
        )

        val tMarkingValue = ActiveFiringTransition.create(
            enabledBindingWithTokens.transition,
            enabledBindingWithTokens.involvedObjectTokens,
            duration = randomSelectedDuration
        )

        tMarking.pushTMarking(tMarkingValue)
        logger.onTransitionStartSectionStart()
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
