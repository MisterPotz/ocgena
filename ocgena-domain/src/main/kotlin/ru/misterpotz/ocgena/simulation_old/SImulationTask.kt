package ru.misterpotz.ocgena.simulation_old

import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation_old.config.MarkingScheme
import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.simulation_old.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation_old.generator.original.TransitionNextInstanceAllowedTimeGeneratorOriginal
import ru.misterpotz.ocgena.simulation_old.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation_old.state.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation_old.logging.SimulationDBLogger
import ru.misterpotz.ocgena.simulation_old.state.original.CurrentSimulationStateOriginal
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
    private val simulationDBLogger: SimulationDBLogger,
    private val simulationTaskPreparator: SimulationTaskPreparator,
    private val currentStateDelegate: CurrentSimulationDelegate,
    private val stepExecutor: SimulationTaskStepExecutor,
    private val developmentDebugConfig: DevelopmentDebugConfig,
    private val executionContinuation: ExecutionContinuation,
    private val dbLogger: ru.misterpotz.Logger,
) : CurrentSimulationDelegate by currentStateDelegate {
    private var finishRequested = false;

    private suspend fun prepare() {
        simulationTaskPreparator.prepare()
        dbLogger.simulationPrepared()
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
        debugStepGranularityLog()

        simulationStepState.currentStep = simulationStepState.stepIndex
        simulationStateProvider.onNewStep()

        simulationDBLogger.onExecutionNewStepStart()

        stepExecutor.executeStep(executionContinuation)

        simulationStepState.incrementStep()
    }

    suspend fun prepareRun() {
        simulationDBLogger.onStart()
        prepare()
        simulationDBLogger.afterInitialMarking()
        simulationStepState.onStart()
    }

    suspend fun doRunStep(): Boolean {
        runStep()
        if (isFinished()) {
            simulationDBLogger.afterFinalMarking()
            simulationStateProvider.markFinished()
            simulationDBLogger.onEnd()
            dbLogger.simulationFinished()
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
