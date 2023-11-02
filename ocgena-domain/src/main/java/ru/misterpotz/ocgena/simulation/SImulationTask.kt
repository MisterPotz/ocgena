package simulation

import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.queue.GenerationQueue
import ru.misterpotz.ocgena.simulation.transition.TransitionInstanceNextCreationTimeGenerator
import javax.inject.Inject

class SimulationTask @Inject constructor(
    private val simulationStateProvider: SimulationStateProvider,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val activityAllowedTimeSelector: TransitionInstanceNextCreationTimeGenerator,
    private val generationQueue: GenerationQueue,
    private val currentStateDelegate: CurrentSimulationDelegate,
    private val stepExecutor: SimulationTaskStepExecutor,
) : CurrentSimulationDelegate by currentStateDelegate {
    private var stepIndex: Long = 0
    private val oneStepGranularity = 5
    var finishRequested = false;

    private fun prepare() {
        ocNet.initialize()

        pMarking.plus(initialMarking)

        for (transition in ocNet.coreOcNet.transitionsRegistry.iterable) {
            val nextAllowedTime = activityAllowedTimeSelector.getNewActivityNextAllowedTime(transition.id)
            state.tTimesMarking.setNextAllowedTime(transition.id, nextAllowedTime)
        }
        generationQueue.planTokenGenerationForEveryone()
    }


    private fun isFinished(): Boolean {
        return executionConditions.checkTerminateConditionSatisfied(runningSimulatableOcNet)
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
