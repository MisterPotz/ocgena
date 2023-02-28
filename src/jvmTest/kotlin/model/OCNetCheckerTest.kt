package model

import dsl.OCScopeImpl
import dsl.createExampleModel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OCNetCheckerTest {
    val ocScopeImpl = createExampleModel()

    @Test
    fun checkConvertionHappensAtAll() {
        val converter = OCNetDSLConverter(ocScopeImpl)
        val result = converter.convert()
    }

    @Test
    fun checkConvertionResults() {
        val converter = OCNetDSLConverter(ocScopeImpl)
        val result = converter.convert()

        assertTrue(result.places.isNotEmpty()) { "converted places were empty " }

        assertTrue(result.places.size == 14) { "incorrect amount of places" }
        assertEquals(24, result.arcs.size) { "incorrect amount of arcs" }
        assertEquals(8, result.transitions.size) { "incorrect amount of transitions" }
        println(result)
    }

    @Test
    fun checkConsistency() {
        val converter = OCNetDSLConverter(ocScopeImpl)
        val result = converter.convert()
        val places = result.places
        val consistencyChecker = OCNetChecker(places)
        val consistencyResults = consistencyChecker.checkConsistency()
        assertTrue(consistencyResults.isEmpty()) {
            "inconsistencies detected :\n" +
                    consistencyResults.joinToString(separator = "\n").prependIndent()
        }
    }
}
