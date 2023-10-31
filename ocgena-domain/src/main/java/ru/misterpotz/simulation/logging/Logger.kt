package simulation

import model.*
import ru.misterpotz.model.marking.Time

interface Logger {

    fun onStart()
    fun afterInitialMarking()

    fun onExecutionNewStepStart()

    fun beforeStartingNewTransitions()
    fun onStartTransition(transition: OngoingActivity)
    fun afterStartingNewTransitions()

    fun beforeEndingTransitions()
    fun onEndTransition(executedBinding: ExecutedBinding)
    fun afterEndingTransitions()

    fun onExecutionStepFinish(newTimeDelta: Time)

    fun afterFinalMarking()

    fun onTimeout()

    fun onEnd()
}
