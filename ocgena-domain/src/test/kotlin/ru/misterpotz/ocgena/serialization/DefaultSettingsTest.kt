package ru.misterpotz.ocgena.serialization

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.DEFAULT_SETTINGS
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.resPath
import ru.misterpotz.ocgena.simulation.config.*
import ru.misterpotz.ocgena.simulation.config.original.Duration
import ru.misterpotz.ocgena.simulation.config.original.TimeUntilNextInstanceIsAllowed
import ru.misterpotz.ocgena.simulation.config.original.TransitionInstanceTimes
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemantics
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.writeOrAssertJson
import ru.misterpotz.ocgena.writeOrAssertYaml
import kotlin.io.path.div

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