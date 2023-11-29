package ru.misterpotz.ocgena.dsl

import org.junit.jupiter.api.Assertions
import java.nio.file.Path

enum class SerializationMode {
    WRITE,
    READ
}

val MODE = SerializationMode.READ

inline fun <reified T> writeOrAssertYaml(
    expected: T,
    path: Path,
    mode: SerializationMode? = null
) {
    val corrMode = mode ?: MODE
    when (corrMode) {
        SerializationMode.WRITE -> {
            val yaml = expected.toYaml()
            yaml.writeConfig(path)
        }
        SerializationMode.READ -> {
            val recordedItem = readConfig<T>(path)
            Assertions.assertEquals(expected, recordedItem)
        }
    }
}

inline fun <reified T> writeOrAssertJson(
    expected: T,
    path: Path,
    mode: SerializationMode? = null
) {
    val corrMode = mode ?: MODE
    when (corrMode) {
        SerializationMode.WRITE -> {
            val json = expected.toJson()
            json.writeConfig(path)
        }
        SerializationMode.READ -> {
            val recordedItem = readConfig<T>(path)
            Assertions.assertEquals(expected, recordedItem)
        }
    }
}