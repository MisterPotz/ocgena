package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
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

    fun settings(): SettingsSimulationConfig {
        return SettingsSimulationConfig(
            initialMarking = initialMarking,
            transitionInstancesTimesSpec = transitionInstancesTimesSpec,
            randomSeed = randomSeed,
            nodeToLabelRegistry = nodeToLabelRegistry,
            tokenGeneration = tokenGeneration,
            ocNetType = ocNetType
        )
    }

    fun withInitialMarking(map: MutableMap<PetriAtomId, Int>.() -> Unit): SimulationConfig {
        val initialMarking = initialMarking.placesToTokens.toMutableMap()
        initialMarking.map()

        return copy(initialMarking = MarkingScheme(initialMarking))
    }

    fun withTransitionsTimes(
        block: MutableMap<PetriAtomId, TransitionInstanceTimes>.() -> Unit
    ): SimulationConfig {
        val updatedTransitionTimes = transitionInstancesTimesSpec.transitionToTimeSpec.toMutableMap()
        updatedTransitionTimes.block()

        return copy(
            transitionInstancesTimesSpec = TransitionInstancesTimesSpec(
                defaultTransitionTimeSpec = transitionInstancesTimesSpec.defaultTransitionTimeSpec,
                transitionToTimeSpec = updatedTransitionTimes
            )
        )
    }

    fun withGenerationPlaces(
        map: MutableMap<PetriAtomId, Int>.() -> Unit
    ): SimulationConfig {
        if (tokenGeneration == null) return this
        val generationPlaces = tokenGeneration.placeIdToGenerationTarget.placesToTokens.toMutableMap()
        generationPlaces.map()

        return copy(
            tokenGeneration = TokenGenerationConfig(
                defaultPeriod = tokenGeneration.defaultPeriod,
                placeIdToGenerationTarget = MarkingScheme(generationPlaces)
            )
        )
    }

    companion object {
        fun fromNetAndSettings(
            ocNet: OCNetStruct,
            settingsSimulationConfig: SettingsSimulationConfig
        ): SimulationConfig {
            return SimulationConfig(
                ocNet = ocNet,
                initialMarking = settingsSimulationConfig.initialMarking,
                transitionInstancesTimesSpec = settingsSimulationConfig.transitionInstancesTimesSpec,
                randomSeed = settingsSimulationConfig.randomSeed,
                nodeToLabelRegistry = settingsSimulationConfig.nodeToLabelRegistry,
                tokenGeneration = settingsSimulationConfig.tokenGeneration,
                ocNetType = settingsSimulationConfig.ocNetType
            )
        }
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
