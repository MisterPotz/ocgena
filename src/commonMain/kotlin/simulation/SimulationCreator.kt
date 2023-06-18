package simulation

import config.GenerationConfig
import kotlinx.serialization.Serializable
import model.*
import model.time.SerializableIntervalFunction
import simulation.random.BindingSelector
import simulation.random.RandomFactory
import simulation.random.TokenSelector

@Serializable
data class SerializableSimulationParams(
    val ocNet: SerializableCoreOcNet,
    val ocNetType: OcNetType,
    val intervalFunction: SerializableIntervalFunction,
    val initialMarking: ImmutableObjectMarking,
    val timeoutSec: Long?,
    val randomSeed: Int?,
    val useRandom: Boolean = true,
//    val labelMapping: LabelMapping,
) {

}

data class SimulationParams(
    val templateOcNet: SimulatableComposedOcNet<*>,
    val initialMarking: ObjectMarking,
    val timeoutSec: Long?,
    val randomSeed: Int?,
    val useRandom: Boolean = true,
    val labelMapping: LabelMapping,
    val objectTokenGenerator: ObjectTokenGenerator,
    val generationConfig: GenerationConfig?,
) {
    val ocNetType: OcNetType
        get() = templateOcNet.ocNetType

    fun toSerializable(): SerializableSimulationParams {
        return SerializableSimulationParams(
            ocNet = templateOcNet.coreOcNet.dumpSerializable(),
            ocNetType = ocNetType,
            intervalFunction = templateOcNet.intervalFunction.toSerializable(),
            initialMarking = initialMarking.toImmutableMarking(),
            timeoutSec = timeoutSec,
            randomSeed, useRandom,
        )
    }
}


class SimulationCreator(
    private val simulationParams: SimulationParams,
    private val executionConditions: ExecutionConditions,
    private val logger: LoggerFactory = LoggerFactoryDefault,
    private val dumpState: Boolean = false
) {
    fun createSimulationTask(): SimulationTask {
        val copy = simulationParams.templateOcNet
        val random = RandomFactory().create(simulationParams.randomSeed).takeIf {
            simulationParams.useRandom
        }

        return SimulationTask(
            simulationParams = simulationParams,
            executionConditions = executionConditions,
            logger = logger.create(labelMapping = simulationParams.labelMapping),
            bindingSelector = BindingSelector(random),
            tokenSelector = TokenSelector(random),
            transitionDurationSelector = TransitionDurationSelector(
                random,
                intervalFunction = copy.intervalFunction
            ),
            transitionInstanceOccurenceDeltaSelector = TransitionInstanceOccurenceDeltaSelector(
                random,
                intervalFunction = copy.intervalFunction
            ),
            tokenNextTimeSelector = TokenGenerationTimeSelector(random),
            dumpState = dumpState,
        )
    }
}
