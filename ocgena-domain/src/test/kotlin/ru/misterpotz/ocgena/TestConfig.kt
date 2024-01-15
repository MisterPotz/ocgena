package ru.misterpotz.ocgena

import org.junit.jupiter.api.Assertions
import java.nio.file.Path

enum class SerializationMode {
    WRITE,
    READ
}

val DEFAULT_SERIALIZATION_TEST_MODE = SerializationMode.READ

val USE_SPECIAL_SYMBOL_OBJ_TYPE_NAME = true

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
    val corrMode = mode ?: DEFAULT_SERIALIZATION_TEST_MODE
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