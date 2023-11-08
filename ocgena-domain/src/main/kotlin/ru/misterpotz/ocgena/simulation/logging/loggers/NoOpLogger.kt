package ru.misterpotz.ocgena.simulation.logging.loggers

import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.simulation.binding.ExecutedBinding
import ru.misterpotz.ocgena.simulation.logging.Logger

open class NoOpLogger : Logger {

    override fun onStart() {

    }
    override fun afterInitialMarking() {}

    override fun onExecutionNewStepStart() {}

    override fun beforeStartingNewTransitions() {}
    override fun onStartTransition(transition: TransitionInstance) {}
    override fun afterStartingNewTransitions() {}

    override fun beforeEndingTransitions() {}
    override fun onEndTransition(executedBinding: ExecutedBinding) {}
    override fun afterEndingTransitions() {}

    override fun onExecutionStepFinish(newTimeDelta: Time) {}

    override fun afterFinalMarking() {}

    override fun onTimeout() {}

    override fun onEnd() {}

    override fun beforeRemovingTokensAtFinishPlace() {
    }

    override fun afterRemovingTokensAtFinishPlace() {
    }
}
