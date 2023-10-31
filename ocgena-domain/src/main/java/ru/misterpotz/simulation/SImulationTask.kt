package simulation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import model.*
import model.typea.SerializableVariableArcTypeA
import model.typel.SerializableArcTypeL
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import ru.misterpotz.model.marking.EmptyObjectValuesMap
import ru.misterpotz.model.marking.ObjectValuesMap
import ru.misterpotz.model.marking.Time
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.simulation.queue.GenerationQueue
import ru.misterpotz.simulation.state.SerializableState
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.transition.TransitionInstanceOccurenceDeltaSelector
import javax.inject.Inject

@Serializable
data class SerializableSimulationState(
    val currentTime: Time,
    val state: SimulatableComposedOcNet.SerializableState,
)

class SimulationTask @Inject constructor(
    private val simulationParams: SimulationConfig,
    simulationTaskStepExecutorFactory: SimulationTaskStepExecutorFactory,
    private val simulationStateProvider: SimulationStateProvider,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val transitionInstanceOccurenceDeltaSelector: TransitionInstanceOccurenceDeltaSelector,
    private val generationQueue: GenerationQueue,
    private val developmentDebugConfig: DevelopmentDebugConfig,
) {
    private val ocNet get() = simulationParams.templateOcNet
    private val simulationTime get() = simulationStateProvider.getSimulationTime()
    private val initialMarking get() = simulationParams.initialMarking
    private val state get() = simulationStateProvider.getOcNetState()
    private val simulationStepState get() = simulationStateProvider.getSimulationStepState()
    private val runningSimulatableOcNet get() = simulationStateProvider.runningSimulatableOcNet()

    private var stepIndex: Long = 0
    private val oneStepGranularity = 5
    var finishRequested = false;

    private val stepExecutor = simulationTaskStepExecutorFactory.createSimulationTaskStepExecutor {
        if (developmentDebugConfig.dumpState) {
            println(
                "\r\ndump after step state: ${simulationStepState.currentStep}: \r\n${
                    dumpState().replace(
                        "\n",
                        "\r\n"
                    )
                }"
            )
        }
    }

    private fun prepare() {
        ocNet.initialize()

        state.pMarking.plus(initialMarking)

        for (transition in ocNet.coreOcNet.transitions) {
            val nextAllowedTime = transitionInstanceOccurenceDeltaSelector.getNewNextOccurrenceTime(transition)
            state.tTimes.setNextAllowedTime(transition, nextAllowedTime)
        }
        generationQueue.planTokenGenerationForEveryone()
    }


    fun isFinished(): Boolean {
        return executionConditions.checkTerminateConditionSatisfied(runningSimulatableOcNet)
                || simulationStepState.isFinished()
                || finishRequested
    }

    fun finish() {
        finishRequested = true
    }

    private fun runStep() {
//        val maxSteps = 10000
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

    private fun dumpState(): String {
//        return yaml.encodeToString(SerializableSimulationState(simulationTime.globalTime, state.toSerializable()))
//            .replace(Regex("\\n[\\s\\r]*\\n"), "\n")
        return ""
    }

    private fun dumpInput(): String {
//        return yaml.encodeToString(simulationParams.toSerializable()).replace(Regex("\\n[\\s\\r]*\\n"), "\n")
        return ""
    }

    fun prepareRun() {
        logger.onStart()
        // always dump net with
        if (developmentDebugConfig.dumpState) {
            println("onStart dump net: ${dumpInput().replace("\n", "\n\r")}")
        }

        prepare()

        if (developmentDebugConfig.dumpState) {
            println("onStart dump state: ${dumpState().replace("\n", "\n\r")}")
        }
        logger.afterInitialMarking()

        simulationStepState.onStart()
    }

    fun doRunStep(): Boolean {
        runStep()
        if (isFinished()) {
            logger.afterFinalMarking(state.pMarking)
            if (developmentDebugConfig.dumpState) {
                println("onFinish dump state: ${dumpState()}")
            }
            println("sending logger onEnd")
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

