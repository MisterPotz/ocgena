package ru.misterpotz

import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.assertTrue

fun simComp() = ServiceProvider.serverComponent

class VisualizeRediscoveredModel {

    @Test
    fun printDotOfConvertedFromPm4py() {
        val file = Path("../ocgena-pm4py-convert/ocnet.yaml")
        assertTrue { file.exists() }

        val string = file.readText()

        val ocNetStruct = simComp().yaml.decodeFromString<OCNetStruct>(string)

        println(ocNetStruct.toDot())
    }
}