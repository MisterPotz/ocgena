package model

import dsl.createExampleModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

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
    fun checkCorrectModelIsConsistent() {
        val converter = OCNetDSLConverter(ocScopeImpl)
        val result = converter.convert()
        val places = result.places
        val consistencyChecker = OCNetChecker(places)
        val consistencyResults = consistencyChecker.checkConsistency()
        assertTrue(consistencyResults.isEmpty()) {
            "inconsistencies detected :\n" +
                    consistencyResults.joinToString(separator = "\n").prependIndent()
        }
        val consistentNet = assertDoesNotThrow {
            consistencyChecker.createConsistentOCNet()
        }
        assertEquals(3, consistentNet.inputPlaces.size)
        assertEquals(3, consistentNet.outputPlaces.size)
        assertEquals(3, consistentNet.objectTypes.size)
    }

    // TODO: create tests for consistency error cases (incorrect model)
}
