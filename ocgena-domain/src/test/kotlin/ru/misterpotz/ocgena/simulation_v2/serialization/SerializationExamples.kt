package ru.misterpotz.ocgena.simulation_v2.serialization

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.SerializationMode
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation_v2.input.*
import ru.misterpotz.ocgena.testing.buildOCNet
import ru.misterpotz.ocgena.writeOrAssertYaml
import kotlin.io.path.Path

class SerializationExamples {
    @Test
    fun simpleLomazovaModel() {
        val model = buildOCNet(ocNetType = OcNetType.LOMAZOVA) {
            "p1".p { input }
            "p2".p { input; objectTypeId = "2" }

            "t1".t { }
            "p1".arc("t1") { norm; multiplicity = 2 }
            "p2".arc("t1") { vari; mathExpr = "n" }

            "p3".p { output }
            "p4".p { output; objectTypeId = "2" }

            "t1".arc("p3") { vari; mathExpr = "2*n" }
            "t1".arc("p4") { vari; mathExpr = "n" }
        }

        println(model.toDot())
        writeOrAssertYaml(model, Path("ocnet_example_lomazova_1.yaml"))
    }

    @Test
    fun simConfigExampleSynchronization() {
        val simulationinput = SimulationInput(
            places = mapOf("input1" to PlaceSetting(initialTokens = 5), "input3" to PlaceSetting(initialTokens = 10)),
            transitions = mapOf(
                "t1" to TransitionSetting(
                    eftLft = Interval(5..10), synchronizedArcGroups = listOf(
                        SynchronizedArcGroup("t2", arcsFromPlaces = listOf("p1", "p2"))
                    )
                )
            ),
            randomSeed = 42,
            defaultEftLft = Interval(10..10),
            loggingEnabled = true
        )

        writeOrAssertYaml<SimulationInput>(simulationinput, Path("siminput_example_sync_1.yaml"))
    }
}