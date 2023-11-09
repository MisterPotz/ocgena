package ru.misterpotz.ocgena.simulation

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation.generator.TransitionNextInstanceAllowedTimeGenerator
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.logging.Logger
import javax.inject.Inject

class SimulationTask @Inject constructor(
    private val simulationStateProvider: SimulationStateProvider,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val activityAllowedTimeSelector: TransitionNextInstanceAllowedTimeGenerator,
    private val newTokenTimeBasedGenerator: NewTokenTimeBasedGenerator,
    private val currentStateDelegate: CurrentSimulationDelegate,
    private val stepExecutor: SimulationTaskStepExecutor,
    private val developmentDebugConfig: DevelopmentDebugConfig,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry
) : CurrentSimulationDelegate by currentStateDelegate {
    private var stepIndex: Long = 0
    private val oneStepGranularity = 5
    var finishRequested = false;

    private fun prepare() {
        initialMarkingScheme.placesToTokens.forEach { (petriAtomId, amount) ->
            objectTokenRealAmountRegistry.incrementRealAmountAt(petriAtomId, amount)
        }
        for (transition in ocNet.ocNet.transitionsRegistry.iterable) {
            val nextAllowedTime = activityAllowedTimeSelector.getNewActivityNextAllowedTime(transition.id)
            state.tTimesMarking.setNextAllowedTime(transition.id, nextAllowedTime)
        }
        newTokenTimeBasedGenerator.planTokenGenerationForEveryone()
    }


    private fun isFinished(): Boolean {
        return executionConditions.checkTerminateConditionSatisfied(ocNet)
                || simulationStepState.isFinished()
                || finishRequested
    }

    fun finish() {
        finishRequested = true
    }

    private fun runStep() {
        var stepsCounter = 0
        while (
            !isFinished() && (stepsCounter++ < oneStepGranularity)
        ) {
            if (stepIndex % developmentDebugConfig.stepNMarkGranularity == 0L && developmentDebugConfig.markEachNStep) {
                println("on step start: $stepIndex")
            }
            simulationStepState.currentStep = stepIndex
            simulationStepState.onNewStep()
            logger.onExecutionNewStepStart()
            stepExecutor.executeStep()
            stepIndex++
        }
    }

    fun prepareRun() {
        logger.onStart()
        prepare()
        logger.afterInitialMarking()
        simulationStepState.onStart()
    }

    fun doRunStep(): Boolean {
        runStep()
        if (isFinished()) {
            logger.afterFinalMarking()
            simulationStateProvider.markFinished()
            logger.onEnd()
            return true
        }
        return false
    }

    fun prepareAndRunAll() {
        prepareRun()
        while (!isFinished()) {
            doRunStep()
        }
    }
}
