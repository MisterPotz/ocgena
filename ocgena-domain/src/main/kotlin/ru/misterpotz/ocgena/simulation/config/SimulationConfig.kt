package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemantics
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType

@Serializable
data class SimulationConfig(
    val ocNet: OCNetStruct,
    @SerialName("init_marking")
    val initialMarking: MarkingScheme,
    @SerialName("transitions")
    val transitionsSpec: TransitionsSpec,
    @SerialName("seed")
    val randomSeed: Int?,
    @SerialName("labels")
    val nodeToLabelRegistry: NodeToLabelRegistry = NodeToLabelRegistry(),
    @SerialName("token_gen")
    val tokenGeneration: TokenGenerationConfig?,
    @SerialName("oc_net_type")
    val ocNetType: OcNetType,
    @SerialName("semantics")
    val simulationSemantics: SimulationSemantics

) {
    fun settingsEqual(settingsSimulationConfig: SettingsSimulationConfig): Boolean {
        return initialMarking == settingsSimulationConfig.initialMarking &&
                transitionsSpec == settingsSimulationConfig.transitionsSpec &&
                randomSeed == settingsSimulationConfig.randomSeed &&
                nodeToLabelRegistry == settingsSimulationConfig.nodeToLabelRegistry &&
                tokenGeneration == settingsSimulationConfig.tokenGeneration &&
                ocNetType == settingsSimulationConfig.ocNetType
    }

    fun settings(): SettingsSimulationConfig {
        return SettingsSimulationConfig(
            initialMarking = initialMarking,
            transitionsSpec = transitionsSpec,
            randomSeed = randomSeed,
            nodeToLabelRegistry = nodeToLabelRegistry,
            tokenGeneration = tokenGeneration,
            ocNetType = ocNetType,
            simulationSemantics = simulationSemantics
        )
    }

    fun withInitialMarking(map: MutableMap<PetriAtomId, Int>.() -> Unit): SimulationConfig {
        val initialMarking = initialMarking.placesToTokens.toMutableMap()
        initialMarking.map()

        return copy(initialMarking = MarkingScheme(initialMarking))
    }

//    fun asTimePNConfig() : SimulationConfig {
//        return copy(
//            simulationSemantics = SimulationSemantics(type = SimulationSemanticsType.SIMPLE_TIME_PN)
//        )
//    }
//
    fun asTimePNwithSpec(timePNSpec: TransitionsTimePNSpec) : SimulationConfig {
        return copy(
            transitionsSpec = timePNSpec,
            simulationSemantics = SimulationSemantics(type = SimulationSemanticsType.SIMPLE_TIME_PN)
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : TransitionsSpec> castTransitions(): T {
        return transitionsSpec as T
    }

    inline fun <reified T : TransitionsSpec> withTransitionsTimes(
        block: T.() -> T
    ): SimulationConfig {
        val t: T = transitionsSpec as T
        val newTransSpec = t.block()

        return copy(
            transitionsSpec = newTransSpec
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
                transitionsSpec = settingsSimulationConfig.transitionsSpec,
                randomSeed = settingsSimulationConfig.randomSeed,
                nodeToLabelRegistry = settingsSimulationConfig.nodeToLabelRegistry,
                tokenGeneration = settingsSimulationConfig.tokenGeneration,
                ocNetType = settingsSimulationConfig.ocNetType,
                simulationSemantics = settingsSimulationConfig.simulationSemantics
            )
        }
    }
}

@Serializable
data class SettingsSimulationConfig(
    @SerialName("init_marking")
    val initialMarking: MarkingScheme,
    @SerialName("transitions")
    val transitionsSpec: TransitionsSpec,
    @SerialName("seed")
    val randomSeed: Int?,
    @SerialName("labels")
    val nodeToLabelRegistry: NodeToLabelRegistry = NodeToLabelRegistry(),
    @SerialName("token_gen")
    val tokenGeneration: TokenGenerationConfig?,
    @SerialName("oc_net_type")
    val ocNetType: OcNetType,
    @SerialName("semantics")
    val simulationSemantics: SimulationSemantics
)
