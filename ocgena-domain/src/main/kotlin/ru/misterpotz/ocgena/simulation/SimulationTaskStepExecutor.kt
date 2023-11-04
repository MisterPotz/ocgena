package ru.misterpotz.ocgena.simulation

import ru.misterpotz.ocgena.simulation.interactors.BindingSelectionInteractor
import ru.misterpotz.ocgena.simulation.interactors.EnabledBindingsCollectorInteractor
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation.state.SimulationStepState
import ru.misterpotz.ocgena.simulation.state.SimulationTime
import ru.misterpotz.ocgena.simulation.structure.RunningSimulatableOcNet
import ru.misterpotz.ocgena.simulation.structure.OcNetInstance
import ru.misterpotz.ocgena.simulation.transition.TransitionTokensLocker
import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation.binding.TIFinisher
import simulation.Logger
import javax.inject.Inject

enum class Status {
    EXECUTING,
    FINISHED
}

interface SimulationStateProvider {
    val status: Status
    fun getOcNetState(): OcNetInstance.State
    fun getSimulationTime(): SimulationTime
    fun getSimulationStepState(): SimulationStepState
    fun runningSimulatableOcNet(): RunningSimulatableOcNet
    fun markFinished()
}

class SimulationStateProviderImpl @Inject constructor(
    simulationConfig: SimulationConfig
) : SimulationStateProvider {
    val state = simulationConfig.ocNetInstance.createInitialState()
    private val simulationTime = SimulationTime()
    private val simulationStepState = SimulationStepState()
    private val runningSimulatableOcNet = RunningSimulatableOcNet(simulationConfig.ocNetInstance, state)
    override var status: Status = Status.EXECUTING

    override fun getOcNetState(): OcNetInstance.State {
        return state
    }

    override fun getSimulationTime(): SimulationTime {
        return simulationTime
    }

    override fun getSimulationStepState(): SimulationStepState {
        return simulationStepState
    }

    override fun runningSimulatableOcNet(): RunningSimulatableOcNet {
        return runningSimulatableOcNet
    }

    override fun markFinished() {
        status = Status.FINISHED
    }
}

class SimulationTaskStepExecutor @Inject constructor(
    private val simulationStateProvider: SimulationStateProvider,
    private val bindingSelectionInteractor: BindingSelectionInteractor,
    private val transitionFinisher: TIFinisher,
    private val transitionTokensLocker: TransitionTokensLocker,
    private val logger: Logger,
    private val newTokenTimeBasedGenerator: NewTokenTimeBasedGenerator,
    private val bindingsCollector: EnabledBindingsCollectorInteractor,
) {
    val state: OcNetInstance.State
        get() = simulationStateProvider.getOcNetState()
    private val simulationTime get() = simulationStateProvider.getSimulationTime()
    private val simulationStepState get() = simulationStateProvider.getSimulationStepState()

    fun executeStep() {
        findAndFinishEndedTransitions()

        generateNewTokensAndPlanNextGeneration()

        findAndStartEnabledTransitionActivities()

        increaseTimeByMinimalSomethingChangingDelta()
    }

    private fun generateNewTokensAndPlanNextGeneration() {
        val markingToAdd = newTokenTimeBasedGenerator.generateTokensAsMarkingAndReplan()
        if (markingToAdd != null) {
            state.pMarking.plus(markingToAdd)
        }
    }

    private fun findAndStartEnabledTransitionActivities() {
        var enabledBindings: List<EnabledBinding> = bindingsCollector.findEnabledBindings()

        logger.beforeStartingNewTransitions()

        if (enabledBindings.isEmpty()) {
            simulationStepState.onHasEnabledTransitions(hasEnabledTransitions = false)
            return
        }
        simulationStepState.onHasEnabledTransitions(hasEnabledTransitions = enabledBindings.isNotEmpty())

        while (enabledBindings.isNotEmpty()) {
            val selectedBinding = bindingSelectionInteractor.selectBinding(enabledBindings)

            val bindingWithTokens = bindingsCollector.resolveEnabledObjectBinding(selectedBinding)

            transitionTokensLocker.lockTokensAndRecordNewTransitionInstance(bindingWithTokens)

            enabledBindings = bindingsCollector.findEnabledBindings()
        }

        logger.afterStartingNewTransitions()
    }

    private fun findAndFinishEndedTransitions() {
        val tMarking = state.tMarking
        val endedTransitions = tMarking.getAndPopEndedTransitions()

        logger.beforeEndingTransitions()
        for (transition in endedTransitions) {
            transitionFinisher.finishActiveTransition(transition)
        }
        logger.afterEndingTransitions()
    }

    private fun increaseTimeByMinimalSomethingChangingDelta() {
        val timeUntilNextFinish = resolveTimeUntilNextTransitionFinish()
        val timeUntilNextEnabledTransition = resolveTimeUntilATransitionIsEnabled()
        val timeUntilNewTokenGenerated = resolveTimeUntilNextTokenGeneration()

        val times = listOf(timeUntilNextFinish, timeUntilNextEnabledTransition, timeUntilNewTokenGenerated)

        val minimumTime = times.filterNotNull().minOrNull()

        simulationStepState.onHasPlannedTransitions(
            hasPlannedTransitions = timeUntilNextEnabledTransition != null
        )
        simulationStepState.onHasPlannedTokens(timeUntilNewTokenGenerated != null)

        if (minimumTime != null) {
            shiftGlobalTime(minimumTime)
            shiftActiveTransitionsByTime(minimumTime)
            shiftTransitionAllowedTime(minimumTime)
            newTokenTimeBasedGenerator.increaseTime(minimumTime)
        }
    }

    private fun resolveTimeUntilNextTokenGeneration(): Time? {
        val time = newTokenTimeBasedGenerator.getTimeUntilNextPlanned()
        return time
    }

    private fun resolveTimeUntilNextTransitionFinish(): Time? {
        val tMarking = state.tMarking
        val earliestFinishActiveTransition = tMarking.getActiveTransitionWithEarliestFinish()
        return earliestFinishActiveTransition?.timeLeftUntilFinish()
    }

    private fun resolveTimeUntilATransitionIsEnabled(): Time? {
        val tTimes = state.tTimesMarking

        val earliestTransitionEnablingTime = tTimes.earliestNonZeroTime()
        return earliestTransitionEnablingTime
    }

    private fun shiftActiveTransitionsByTime(time: Time) {
        val tMarking = state.tMarking
        tMarking.shiftByTime(time)
    }

    private fun shiftTransitionAllowedTime(time: Time) {
        val tTimes = state.tTimesMarking
        tTimes.increaseSimTime(time)
    }

    private fun shiftGlobalTime(time: Time) {
        simulationTime.shiftByDelta(time)
    }
}
