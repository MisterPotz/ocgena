package ru.misterpotz.ocgena.simulation_old.logging

import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.simulation_old.binding.ExecutedBinding
import ru.misterpotz.ocgena.simulation_old.collections.TransitionInstance

interface SimulationDBLogger {

    fun onStart()
    fun afterInitialMarking()

    fun onExecutionNewStepStart()

    fun beforeStartingNewTransitions()
    fun onStartTransition(transition: TransitionInstance)
    fun afterStartingNewTransitions()

    fun beforeEndingTransitions()
    fun onEndTransition(executedBinding: ExecutedBinding)
    fun afterEndingTransitions()

    fun beforeRemovingTokensAtFinishPlace()

    fun afterRemovingTokensAtFinishPlace()

    fun onExecutionStepFinish(newTimeDelta: Time)

    fun afterFinalMarking()

    fun onTimeout()

    fun onEnd()
}
