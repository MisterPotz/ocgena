package simulation

import ru.misterpotz.marking.objects.Time
import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.simulation.binding.ExecutedBinding

interface Logger {

    fun onStart()
    fun afterInitialMarking()

    fun onExecutionNewStepStart()

    fun beforeStartingNewTransitions()
    fun onStartTransition(transition: TransitionInstance)
    fun afterStartingNewTransitions()

    fun beforeEndingTransitions()
    fun onEndTransition(executedBinding: ExecutedBinding)
    fun afterEndingTransitions()

    fun onExecutionStepFinish(newTimeDelta: Time)

    fun afterFinalMarking()

    fun onTimeout()

    fun onEnd()
}
