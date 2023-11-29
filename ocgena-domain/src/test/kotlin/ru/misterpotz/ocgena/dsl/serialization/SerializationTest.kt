package ru.misterpotz.ocgena.dsl.serialization

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.dsl.buildOCNet
import ru.misterpotz.ocgena.dsl.defaultSimConfig
import ru.misterpotz.ocgena.dsl.writeOrAssertJson
import ru.misterpotz.ocgena.dsl.writeOrAssertYaml
import kotlin.io.path.Path

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
    private val json_path = Path("serialization", "serialized_with_types.json")
    private val yaml_path = Path("serialization", "serialized_with_types.yaml")
    private val json_settings_path = Path("serialization", "serialized_settings.json")
    private val yaml_settings_path = Path("serialization", "serialized_settings.yaml")

    @Test
    fun jsonInAndOUt() {
        writeOrAssertJson(simCOnfig, json_path)
    }

    @Test
    fun yamlInAndOut() {
        writeOrAssertYaml(simCOnfig, yaml_path)
    }

    @Test
    fun `merging model and settings works for json`() {
        writeOrAssertJson(simCOnfig.settings(), json_settings_path)
    }

    @Test
    fun `merging model and settings works for yaml`() {
        writeOrAssertJson(simCOnfig.settings(), yaml_settings_path)
    }
}
