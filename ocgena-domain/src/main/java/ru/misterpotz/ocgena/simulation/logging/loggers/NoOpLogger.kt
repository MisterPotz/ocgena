package simulation.client.loggers

import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.simulation.binding.ExecutedBinding
import ru.misterpotz.marking.objects.Time
import simulation.Logger

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
}
