package ru.misterpotz.ocgena.simulation_v2

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.SerializationMode
import ru.misterpotz.ocgena.simulation_v2.input.*
import ru.misterpotz.ocgena.testing.buildOCNet
import ru.misterpotz.ocgena.writeOrAssertYaml
import kotlin.io.path.Path

class ConfigSerializationTest {

    @Test
    fun simpleModel() {
        buildOCNet {
            "input1".p { input }
            "input2".p { input }
            "input3".p { input }
            "input4".p { input; }
            "t2".t
            "input1".arc("t2")
            "input2".arc("t2")
            "p1".p { objectTypeId = "1" }
            "t2".arc("p1")
            "p1".arc("t1".t) { vari; }
            "p2".p
            "input3".arc("t3".t).arc("p2")
            "t2".arc("p2")
            "p2".arc("t1")
            "t1".t.arc("output".p { output })
            "input4".arc("t1")
        }.let { println(it.toDot()) }
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