package ru.misterpotz.ocgena

import org.junit.jupiter.api.Assertions
import java.nio.file.Path

enum class SerializationMode {
    WRITE,
    READ
}

val DEFAULT_SERIALIZATION_TEST_MODE = SerializationMode.READ

val USE_SPECIAL_SYMBOL_OBJ_TYPE_NAME = true
