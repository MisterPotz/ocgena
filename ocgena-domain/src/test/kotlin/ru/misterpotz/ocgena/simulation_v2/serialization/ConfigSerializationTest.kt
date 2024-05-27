package ru.misterpotz.ocgena.simulation_v2.serialization

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.SerializationMode
import ru.misterpotz.ocgena.simulation_v2.input.*
import ru.misterpotz.ocgena.testing.build3Tran4InpExample
import ru.misterpotz.ocgena.writeOrAssertYaml
import kotlin.io.path.Path

class ConfigSerializationTest {

    @Test
    fun simpleModel() {
        println(build3Tran4InpExample().toDot())
    }

    @Test
    fun test() {
        val simulationinput = SimulationInput(
            places = mapOf("p1" to PlaceSetting(initialTokens = 5)),
            transitions = mapOf(
                "t1" to TransitionSetting(
                    eftLft = Interval(5..10), synchronizedArcGroups = listOf(
                        SynchronizedArcGroup("t2", arcsFromPlaces = listOf("p1", "p2"))
                    )
                )
            ),
            randomSeed = 42,
            loggingEnabled = true
        )

        writeOrAssertYaml<SimulationInput>(simulationinput, Path("v2_config.yaml"), SerializationMode.WRITE)
    }
}