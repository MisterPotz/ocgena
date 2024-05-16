package ru.misterpotz.ocgena.serialization

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.DEFAULT_SETTINGS
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation_old.config.*
import ru.misterpotz.ocgena.simulation_old.config.original.Duration
import ru.misterpotz.ocgena.simulation_old.config.original.TimeUntilNextInstanceIsAllowed
import ru.misterpotz.ocgena.simulation_old.config.original.TransitionInstanceTimes
import ru.misterpotz.ocgena.simulation_old.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation_old.semantics.SimulationSemantics
import ru.misterpotz.ocgena.simulation_old.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.writeOrAssertYaml

class DefaultSettingsTest {
    val defaultSettings = SettingsSimulationConfig(
        initialMarking = MarkingScheme.of { },
        transitionsSpec = TransitionsOriginalSpec(
            defaultTransitionTimeSpec = TransitionInstanceTimes(
                duration = Duration(10..10),
                timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(
                    0..0
                )
            ),
            transitionToTimeSpec = mutableMapOf(),
        ),
        randomSeed = 42,
        nodeToLabelRegistry = NodeToLabelRegistry(),
        tokenGeneration = TokenGenerationConfig(
            defaultPeriod = Period(25..25),
            placeIdToGenerationTarget = MarkingScheme.of { },
        ),
        ocNetType = OcNetType.AALST,
        simulationSemantics = SimulationSemantics(
            type = SimulationSemanticsType.ORIGINAL
        )
    )

    @Test
    fun defaultSettingsTest() {
        writeOrAssertYaml(defaultSettings, path = DEFAULT_SETTINGS)
    }
}