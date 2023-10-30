package simulation

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import model.*
import model.typea.SerializableVariableArcTypeA
import model.typel.SerializableArcTypeL
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlBuilder
import simulation.binding.ActiveTransitionFinisherImpl
import simulation.binding.BindingOutputMarkingResolverFactory
import simulation.random.BindingSelector
import simulation.random.TokenSelector
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
data class SerializableSimulationState(
    val currentTime: Time,
    val state: SimulatableComposedOcNet.SerializableState,
)

class SimulationTask(
    private val simulationParams: SimulationParams,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val bindingSelector: BindingSelector,
    private val tokenSelector: TokenSelector,
    private val transitionDurationSelector: TransitionDurationSelector,
    private val transitionInstanceOccurenceDeltaSelector: TransitionInstanceOccurenceDeltaSelector,
    private val tokenNextTimeSelector: TokenGenerationTimeSelector,
    private val dumpState: Boolean = false,
) {
    private val ocNet = simulationParams.templateOcNet
    private val simulationTime = SimulationTime()
    private val initialMarking = simulationParams.initialMarking
    private val duration = (simulationParams.timeoutSec ?: 30L).toDuration(DurationUnit.SECONDS)
    private val state = ocNet.createInitialState()

    private val runningSimulatableOcNet = RunningSimulatableOcNet(ocNet, state)
    private val simulationState = SimulationState()
    val generationQueue = simulationParams.generationConfig?.let {
        NormalGenerationQueue(
            generationConfig = it,
            nextTimeSelector = tokenNextTimeSelector,
            placeTyping = ocNet.coreOcNet.placeTyping,
            tokenGenerator = simulationParams.objectTokenGenerator,
        )
    } ?: DumbGenerationQueue()

    private val stepExecutor = SimulationTaskStepExecutor(
        ocNet,
        state,
        bindingSelector,
        transitionFinisher = ActiveTransitionFinisherImpl(
            state.pMarking,
            inputToOutputPlaceResolver = BindingOutputMarkingResolverFactory(
                arcs = ocNet.coreOcNet.arcs,
                ocNetType = ocNet.ocNetType,
                placeTyping = ocNet.coreOcNet.placeTyping,
                objectTokenGenerator = simulationParams.objectTokenGenerator,
                objectTokenMoverFactory = ObjectTokenMoverFactory(tokenSelector, ocNetType = simulationParams.ocNetType)
            ).create(),
            logger,
            simulationTime
        ),
        simulationState = simulationState,
        simulationTime = simulationTime,
        logger = logger,
        tokenSelector = tokenSelector,

        transitionDurationSelector = transitionDurationSelector,
        nextTransitionOccurenceTimeSelector = transitionInstanceOccurenceDeltaSelector,
        generationConfig = simulationParams.generationConfig,
        nextTimeSelector = tokenNextTimeSelector,
        tokenGenerator = simulationParams.objectTokenGenerator,
        placeTyping = ocNet.coreOcNet.placeTyping,
        generationQueue = generationQueue,
        dumpState = {
            if (dumpState) {
                println(
                    "\r\ndump after step state: ${simulationState.currentStep}: \r\n${
                        dumpState().replace(
                            "\n",
                            "\r\n"
                        )
                    }"
                )
            }
        }
    )

    private fun prepare() {
        ocNet.initialize()

        state.pMarking += initialMarking

        for (transition in ocNet.coreOcNet.transitions) {
            val nextAllowedTime = transitionInstanceOccurenceDeltaSelector.getNewNextOccurrenceTime(transition)
            state.tTimes.setNextAllowedTime(transition, nextAllowedTime)
        }
        generationQueue.planTokenGenerationForEveryone()
    }

    private var stepIndex: Int = 0
    private val oneStepGranularity = 5
    var finishRequested = false;
    fun isFinished() : Boolean {
        return executionConditions.checkTerminateConditionSatisfied(runningSimulatableOcNet)
                || simulationState.isFinished()
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
            simulationState.currentStep = stepIndex
            simulationState.onNewStep()

            logger.onExecutionStepStart(stepIndex, state, simulationTime)

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
        if (dumpState) {
            println("onStart dump net: ${dumpInput().replace("\n", "\n\r")}")
        }

        prepare()

        if (dumpState) {
            println("onStart dump state: ${dumpState().replace("\n", "\n\r")}")
        }
        logger.onInitialMarking(state.pMarking)

        simulationState.onStart()
    }

    fun doRunStep() : Boolean {
        runStep()
        if (isFinished()) {
            logger.onFinalMarking(state.pMarking)
            if (dumpState) {
                println("onFinish dump state: ${dumpState()}")
            }
            println("sending logger onEnd")
            logger.onEnd()
            return true
        }
        return false
    }

    fun prepareAndRunAll() {
        prepareRun()
        while(!isFinished()) {
            doRunStep()
        }
    }
}

val yaml = Yaml {
    this.listSerialization = YamlBuilder.ListSerialization.AUTO
    this.mapSerialization = YamlBuilder.MapSerialization.BLOCK_MAP

    serializersModule = SerializersModule {
        polymorphic(baseClass = SerializableAtom::class) {
            subclass(SerializablePlace::class, SerializablePlace.serializer())
            subclass(SerializableTransition::class, SerializableTransition.serializer())
            subclass(SerializableNormalArc::class, SerializableNormalArc.serializer())
            subclass(SerializableArcTypeL::class, SerializableArcTypeL.serializer())
            subclass(SerializableVariableArcTypeA::class, SerializableVariableArcTypeA.serializer())
        }

        polymorphic(SimulatableComposedOcNet.SerializableState::class) {
            subclass(SerializableState::class, SerializableState.serializer())
        }
        polymorphic(ObjectValuesMap::class) {
            subclass(EmptyObjectValuesMap::class, EmptyObjectValuesMap.serializer())
        }
    }
}
