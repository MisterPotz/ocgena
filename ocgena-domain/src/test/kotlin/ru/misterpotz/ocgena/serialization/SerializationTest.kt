package ru.misterpotz.ocgena.serialization

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.testing.buildOCNet
import ru.misterpotz.ocgena.testing.defaultSimConfigOriginal
import ru.misterpotz.ocgena.testing.defaultSimConfigTimePN
import java.util.stream.Stream
import kotlin.io.path.Path


class SerializationTest {

    class TestArgs(prepath: String, val config: SimulationConfig) {
        val json_path = Path("$prepath/serialization", "serialized_settings_with_net.json")
        val yaml_path = Path("$prepath/serialization", "serialized_settings_with_net.yaml")
        val json_settings_path = Path("$prepath/serialization", "serialized_settings.json")
        val yaml_settings_path = Path("$prepath/serialization", "serialized_settings.yaml")
    }

    @ParameterizedTest(name = "json_full")
    @ArgumentsSource(ArgsProvider::class)
    @Disabled
    fun jsonInAndOUt(paths: TestArgs) {
        writeOrAssertJson(paths.config, paths.json_path)
    }

    @ParameterizedTest(name = "yaml_full")
    @ArgumentsSource(ArgsProvider::class)
    @Disabled
    fun yamlInAndOut(paths: TestArgs) {
        writeOrAssertYaml(paths.config, paths.yaml_path)
    }


    @ParameterizedTest(name = "json_settings")
    @ArgumentsSource(ArgsProvider::class)
    fun `merging model and settings works for json`(paths: TestArgs) {
        writeOrAssertJson(paths.config.settings(), paths.json_settings_path)
    }

    @ParameterizedTest(name = "yaml_settings")
    @ArgumentsSource(ArgsProvider::class)
    fun `merging model and settings works for yaml`(paths: TestArgs) {
        writeOrAssertYaml(paths.config.settings(), paths.yaml_settings_path)
    }

    companion object {
        private val simpleOcNet = buildOCNet {
            "p1".p { input; }
                .arc("t1".t)
                .arc("p2".p { output })

            "o1".p { input; objectTypeId = "2" }
                .arc("t1".t) { vari }
                .arc("o2".p { output; objectTypeId = "2" }) { vari }
        }
        private val simCOnfig = defaultSimConfigOriginal(
            ocNet = simpleOcNet
        )
        private val simConfigTime = defaultSimConfigTimePN(ocNet = simpleOcNet)

        private class ArgsProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
                return Stream.of(
                    Arguments.of(TestArgs("original", simCOnfig)),
                    Arguments.of(TestArgs("timepn", simConfigTime))
                )
            }

        }
    }
}
