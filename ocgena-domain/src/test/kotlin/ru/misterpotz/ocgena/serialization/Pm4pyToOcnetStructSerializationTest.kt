package ru.misterpotz.ocgena.serialization

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.yamlConfig
import kotlin.io.path.Path

class Pm4pyToOcnetStructSerializationTest {

    @Test
    fun `pm4py mapped yaml maps normally into ocnet struct`() {
        val deserialized = yamlConfig<OCNetStruct>("pm4py_to_ocnet/ocnet.yaml")
        deserialized.objectTypeRegistry
    }
}