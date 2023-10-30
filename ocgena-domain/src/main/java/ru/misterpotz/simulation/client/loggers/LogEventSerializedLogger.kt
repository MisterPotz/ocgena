package ru.misterpotz.simulation.client.loggers

import model.ActiveFiringTransition
import model.ExecutedBinding
import ru.misterpotz.model.ObjectMarking
import model.Time
import ru.misterpotz.simulation.state.SimulationTime
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import simulation.SimulationStateProvider
import simulation.client.loggers.NoOpLogger
import javax.inject.Inject

class LogEventSerializedLogger @Inject constructor(
    private val simulationStateProvider: SimulationStateProvider
) : NoOpLogger() {

    fun onStart() {

    }

    override fun onInitialMarking(marking: ObjectMarking) {
        super.onInitialMarking(marking)
    }

    override fun onFinalMarking(marking: ObjectMarking) {
        super.onFinalMarking(marking)
    }

    override fun onEnd() {
        super.onEnd()
    }

    override fun onTimeout() {
        super.onTimeout()
    }

    override fun onTimeShift(delta: Time) {
        super.onTimeShift(delta)
    }

    override fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime
    ) {
        super.onExecutionStepStart(stepIndex, state, simulationTime)
    }

    override fun onTransitionEndSectionStart() {
        super.onTransitionEndSectionStart()
    }

    override fun beforeStartingNewTransitions() {
        super.beforeStartingNewTransitions()
    }

    override fun onEndTransition(executedBinding: ExecutedBinding) {
        super.onEndTransition(executedBinding)
    }

    override fun onStartTransition(transition: ActiveFiringTransition) {
        super.onStartTransition(transition)
    }
}