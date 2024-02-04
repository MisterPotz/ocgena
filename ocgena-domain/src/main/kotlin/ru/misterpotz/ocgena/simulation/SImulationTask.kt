package ru.misterpotz.ocgena.simulation

import ru.misterpotz.DBLogger
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation.generator.original.TransitionNextInstanceAllowedTimeGeneratorOriginal
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.state.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.logging.Logger
import ru.misterpotz.ocgena.simulation.state.original.CurrentSimulationStateOriginal
import javax.inject.Inject


interface SimulationTaskPreparator {
    fun prepare()
}

class SimulationTaskPreparatorOriginal(
    val simulationConfig: SimulationConfig,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    private val activityAllowedTimeSelector: TransitionNextInstanceAllowedTimeGeneratorOriginal,
    private val newTokenTimeBasedGenerator: NewTokenTimeBasedGenerator,
    private val currentSimulationStateOriginal: CurrentSimulationStateOriginal
) : SimulationTaskPreparator {
    override fun prepare() {
        (simulationConfig.initialMarking ?: MarkingScheme()).placesToTokens.forEach { (petriAtomId, amount) ->
            objectTokenRealAmountRegistry.incrementRealAmountAt(petriAtomId, amount)
        }
        for (transition in simulationConfig.ocNet.transitionsRegistry.iterable) {
            val nextAllowedTime = activityAllowedTimeSelector.getNewActivityNextAllowedTime(transition.id)
            currentSimulationStateOriginal.tTimesMarking.setNextAllowedTime(transition.id, nextAllowedTime)
        }
        newTokenTimeBasedGenerator.planTokenGenerationForEveryone()

    }
}

class SimulationTask @Inject constructor(
    private val simulationStateProvider: SimulationStateProvider,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val simulationTaskPreparator: SimulationTaskPreparator,
    private val currentStateDelegate: CurrentSimulationDelegate,
    private val stepExecutor: SimulationTaskStepExecutor,
    private val developmentDebugConfig: DevelopmentDebugConfig,
    private val executionContinuation: ExecutionContinuation,
    private val dbLogger: DBLogger,
) : CurrentSimulationDelegate by currentStateDelegate {
    private var finishRequested = false;

    private fun prepare() {
        simulationTaskPreparator.prepare()
    }


    private fun isFinished(): Boolean {
        return executionConditions.checkTerminateConditionSatisfied(ocNet)
                || simulationStepState.isFinished()
                || finishRequested
    }

    fun finish() {
        finishRequested = true
    }

    private fun debugStepGranularityLog() {
        if (simulationStepState.stepIndex % developmentDebugConfig.stepNMarkGranularity == 0L &&
            developmentDebugConfig.markEachNStep
        ) {
            println("on step start: ${simulationStepState.stepIndex}")
        }
    }

    private suspend fun runStep() {
        while (
            !isFinished() &&
            executionContinuation.shouldDoNextStep()
        ) {
            debugStepGranularityLog()

            simulationStepState.currentStep = simulationStepState.stepIndex
            simulationStateProvider.onNewStep()

            logger.onExecutionNewStepStart()

            stepExecutor.executeStep(executionContinuation)

            simulationStepState.incrementStep()
        }
        dbLogger.simulationFinished()
    }

    fun prepareRun() {
        logger.onStart()
        prepare()
        logger.afterInitialMarking()
        simulationStepState.onStart()
    }

    suspend fun doRunStep(): Boolean {
        runStep()
        if (isFinished()) {
            logger.afterFinalMarking()
            simulationStateProvider.markFinished()
            logger.onEnd()
            return true
        }
        return false
    }

    suspend fun prepareAndRunAll() {
        prepareRun()
        while (!isFinished()) {
            doRunStep()
        }
    }
}
