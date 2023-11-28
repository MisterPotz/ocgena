package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry

@Serializable
data class SimulationConfig(
    val ocNet: OCNetStruct,
    @SerialName("init_marking")
    val initialMarking: MarkingScheme,
    @SerialName("transition_times")
    val transitionInstancesTimesSpec: TransitionInstancesTimesSpec,
    @SerialName("seed")
    val randomSeed: Int?,
    @SerialName("labels")
    val nodeToLabelRegistry: NodeToLabelRegistry = NodeToLabelRegistry(),
    @SerialName("token_gen")
    val tokenGeneration: TokenGenerationConfig?,
    @SerialName("oc_net_type")
    val ocNetType: OcNetType,
) {
    fun settingsEqual(settingsSimulationConfig: SettingsSimulationConfig): Boolean {
        return initialMarking == settingsSimulationConfig.initialMarking &&
                transitionInstancesTimesSpec == settingsSimulationConfig.transitionInstancesTimesSpec &&
                randomSeed == settingsSimulationConfig.randomSeed &&
                nodeToLabelRegistry == settingsSimulationConfig.nodeToLabelRegistry &&
                tokenGeneration == settingsSimulationConfig.tokenGeneration &&
                ocNetType == settingsSimulationConfig.ocNetType
    }
}

@Serializable
data class SettingsSimulationConfig(
    @SerialName("init_marking")
    val initialMarking: MarkingScheme,
    @SerialName("transition_times")
    val transitionInstancesTimesSpec: TransitionInstancesTimesSpec,
    @SerialName("seed")
    val randomSeed: Int?,
    @SerialName("labels")
    val nodeToLabelRegistry: NodeToLabelRegistry = NodeToLabelRegistry(),
    @SerialName("token_gen")
    val tokenGeneration: TokenGenerationConfig?,
    @SerialName("oc_net_type")
    val ocNetType: OcNetType,
)
