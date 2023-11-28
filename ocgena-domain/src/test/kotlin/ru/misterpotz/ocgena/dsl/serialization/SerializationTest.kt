package ru.misterpotz.ocgena.dsl.serialization

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.dsl.tool.*
import ru.misterpotz.ocgena.simulation.config.SettingsSimulationConfig
import ru.misterpotz.ocgena.simulation.config.SimulationConfig

class SerializationTest {
    private val simpleOcNet = buildOCNet {
        "p1".p { input; }
            .arc("t1".t)
            .arc("p2".p { output })

        "o1".p { input; objectTypeId = "2" }
            .arc("t1".t) { vari }
            .arc("o2".p { output; objectTypeId = "2" }) { vari }
    }
    private val simCOnfig = defaultSimConfig(
        ocNet = simpleOcNet
    )
    private val json_path = "serialized_with_types.json"
    private val yaml_path = "serialized_with_types.yaml"
    private val json_settings_path = "serialized_settings.json"
    private val yaml_settings_path = "serialized_settings.yaml"

    @Test
    fun jsonSerialization() {
        Assertions.assertTrue(simCOnfig.toJson().isNotBlank())
    }

    @Test
    fun yamlSerialization() {
        Assertions.assertTrue(simCOnfig.toYaml().isNotBlank())
    }

    @Test
    fun jsonSimpleDeserialization() {
        val decoded = readConfig<SimulationConfig>(json_path)

        Assertions.assertEquals(simCOnfig, decoded)
    }

    @Test
    fun yamlSimpleDeserialization() {
        val decoded = readConfig<SimulationConfig>(yaml_path)

        Assertions.assertEquals(simCOnfig, decoded)
    }

    @Test
    fun `merging model and settings works for json`() {
        val settings = readConfig<SettingsSimulationConfig>(json_settings_path)

        Assertions.assertTrue(simCOnfig.settingsEqual(settings))
    }

    @Test
    fun `merging model and settings works for yaml`() {
        val settings = readConfig<SettingsSimulationConfig>(yaml_settings_path)

        Assertions.assertTrue(simCOnfig.settingsEqual(settings))
    }
}
