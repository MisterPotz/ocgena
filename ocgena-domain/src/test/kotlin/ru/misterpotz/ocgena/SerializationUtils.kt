package ru.misterpotz.ocgena

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.original.TestFolder
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.config.SettingsSimulationConfig
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemantics
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.testing.DOMAIN_COMPONENT
import ru.misterpotz.ocgena.utils.findInstance
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.pathString

val DEFAULT_SETTINGS = Path("default_settings.yaml")
val resPathString = "src/test/resources/"
val res = File(resPathString)
val resPath = Path(resPathString)


inline fun <reified T> writeOrAssertYaml(
    expected: T,
    path: Path,
    mode: SerializationMode? = null
) {
    val corrMode = mode ?: DEFAULT_SERIALIZATION_TEST_MODE
    when (corrMode) {
        SerializationMode.WRITE -> {
            val yaml = expected.toYaml()
            yaml.writeConfig(path)
        }

        SerializationMode.READ -> {
            val recordedItem = deserializeResourceByClassWithPath<T>(path)
            Assertions.assertEquals(expected, recordedItem)
        }
    }
}

inline fun <reified T> writeOrAssertJson(
    expected: T,
    path: Path,
    mode: SerializationMode? = null
) {
    val corrMode = mode ?: DEFAULT_SERIALIZATION_TEST_MODE
    when (corrMode) {
        SerializationMode.WRITE -> {
            val json = expected.toJson()
            json.writeConfig(path)
        }

        SerializationMode.READ -> {
            val recordedItem = deserializeResourceByClassWithPath<T>(path)
            Assertions.assertEquals(expected, recordedItem)
        }
    }
}

fun readAndBuildConfig(
    settingsPath: Path = DEFAULT_SETTINGS,
    modelPath: ModelPath,
): SimulationConfig {
    val defaultSettings = deserializeResourceByClassWithPath<SettingsSimulationConfig>(settingsPath)
    val model = modelPath.load()

    return SimulationConfig.fromNetAndSettings(
        model,
        defaultSettings
    )
}

fun config(name: String): File {
    return File(res, name)
}

fun config(path: Path): File {
    return path.toFile()
}

fun String.writeConfig(path: Path) {
    val pathCorrected = appendPathWithRes(path = path)
    pathCorrected.toFile().writeText(this)
}

inline fun <reified T> jsonConfig(name: String): T {
    val text = config(name).readText()

    return DOMAIN_COMPONENT.json.decodeFromString<T>(text)
}

inline fun <reified T> yamlConfig(name: String): T {
    val text = config(name).readText()

    return DOMAIN_COMPONENT.yaml.decodeFromString<T>(text)
}

inline fun <reified T> jsonConfig(path: Path): T {

    val text = config(path).readText()

    return DOMAIN_COMPONENT.json.decodeFromString<T>(text)
}

inline fun <reified T> yamlConfig(path: Path): T {
    val text = config(path).readText()

    return DOMAIN_COMPONENT.yaml.decodeFromString<T>(text)
}

inline fun <reified T> T.toYaml(): String {
    return DOMAIN_COMPONENT.yaml.encodeToString(this)
}

inline fun <reified T> T.toJson(): String {
    return DOMAIN_COMPONENT.json.encodeToString(this)
}

inline fun <reified T> deserializeResourceByClassWithPath(name: String): T {
    return if (name.endsWith(".json")) {
        jsonConfig<T>(name)
    } else if (name.endsWith(".yaml")) {
        yamlConfig<T>(name)
    } else {
        throw IllegalStateException()
    }
}

fun appendPathWithRes(path: Path): Path {
    val resPath = Path(resPathString)
    return if (path.first() == resPath) {
        path
    } else {
        resPath / path
    }
}

inline fun <reified T> deserializeResourceByClassWithPath(path: Path): T {
    val path = appendPathWithRes(path = path)

    return if (path.pathString.endsWith(".json")) {
        jsonConfig<T>(path)
    } else if (path.pathString.endsWith(".yaml")) {
        yamlConfig<T>(path)
    } else {
        throw IllegalStateException()
    }
}

inline fun <reified T> readFolderConfig(folderName: String, name: String): T {
    val withFolderPath = Path(resPathString) / folderName / name

    val string = withFolderPath.pathString
    return if (string.endsWith(".json")) {
        jsonConfig<T>(withFolderPath)
    } else if (string.endsWith(".yaml")) {
        yamlConfig<T>(withFolderPath)
    } else {
        throw IllegalStateException()
    }
}

inline fun <reified T, R : Any> T.withFolderName(action: (String) -> R): R {
    val folderName = this!!::class.annotations.findInstance<TestFolder>()!!.folderName
    return action(folderName)
}

inline fun <reified T, reified R : Any> T.readFolderConfig(name: String): R {
    return withFolderName {
        readFolderConfig<R>(it, name)
    }
}