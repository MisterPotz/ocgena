package ru.misterpotz.ocgena.dsl.serialization

import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.dsl.tool.buildOCNet
import ru.misterpotz.ocgena.dsl.tool.defaultSimConfig
import ru.misterpotz.ocgena.dsl.tool.domainComponent
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import java.io.File

class SerializationTest {
    val simpleOcNet = buildOCNet {
        "p1".p { input; }
            .arc("t1".t)
            .arc("p2".p { output })

        "o1".p { input; objectTypeId = "2" }
            .arc("t1".t) { vari }
            .arc("o2".p { output; objectTypeId = "2" }) { vari }
    }
    val simCOnfig = defaultSimConfig(
        ocNet = simpleOcNet
    )

    @Test
    fun jsonSerialization() {
        val serialized = domainComponent()
        val string = serialized.json().encodeToString(SimulationConfig.serializer(), simCOnfig)
        Assertions.assertTrue(string.isNotBlank())
    }

    @Test
    fun yamlSerialization() {
        val serialized = domainComponent()
        val string = serialized.yaml.encodeToString(SimulationConfig.serializer(), simCOnfig)
        Assertions.assertTrue(string.isNotBlank())
    }

    @Test
    fun jsonSimpleDeserialization() {
        val comp = domainComponent()
        val path = "src/test/resources/serialized_with_types.json"
        val file = File(path)

        val json = file.inputStream().bufferedReader().readText()

        val decoded = comp.json.decodeFromString<SimulationConfig>(json)

        Assertions.assertEquals(simCOnfig, decoded)
    }

    @Test
    fun yamlSimpleDeserialization() {
        val comp = domainComponent()
        val path = "src/test/resources/serialized_with_types.yaml"
        val file = File(path)

        val yaml = file.inputStream().bufferedReader().readText()

        val decoded = comp.yaml.decodeFromString<SimulationConfig>(yaml)

        Assertions.assertEquals(simCOnfig, decoded)
    }
}
