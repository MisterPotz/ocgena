package model

import dsl.OCNetBuilder
import dsl.OCScope
import dsl.createExampleModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.reflect.jvm.internal.impl.protobuf.ByteString.Output
import kotlin.test.assertNotNull

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
    }

    @Test
    fun checkCorrectModelIsConsistent() {
        val converter = OCNetDSLConverter(ocScopeImpl)
        val result = converter.convert()
        val places = result.places
        val consistencyChecker = OCNetChecker(result.allPetriNodes)
        val consistencyResults = consistencyChecker.checkConsistency()
        assertTrue(consistencyResults.isEmpty()) {
            "inconsistencies detected:\n" + consistencyResults.joinToString(separator = "\n").prependIndent()
        }
        val consistentNet = assertDoesNotThrow {
            consistencyChecker.createConsistentOCNet()
        }
        assertEquals(3, consistentNet.inputPlaces.size)
        assertEquals(3, consistentNet.outputPlaces.size)
        assertEquals(3, consistentNet.objectTypes.size)
    }

    private fun createAndCheckForConsistency(block: OCScope.() -> Unit): List<ConsistencyCheckError> {
        val ocScope = OCNetBuilder.define(block)
        val converter = OCNetDSLConverter(ocScope)
        val convertionResult = converter.convert()
        val ocnetChecker = OCNetChecker(convertionResult.allPetriNodes)
        return ocnetChecker.checkConsistency()
    }

    @Test
    fun checkInputOutputAbsence() {
        val errors = createAndCheckForConsistency {
            // place types not specified
            place { } arcTo transition { } arcTo place { }
        }
        assertTrue(errors.isNotEmpty()) { "errors were expected " }
        assertNotNull(errors.find { it is ConsistencyCheckError.NoInputPlacesDetected }) { "isolated error expected" }
        assertNotNull(errors.find { it is ConsistencyCheckError.NoOutputPlacesDetected }) { "isolated error expected" }
    }

    @Test
    fun checkIsolatedSubgraphPresent() {
        val errors = createAndCheckForConsistency {
            // isolated place
            place { }
            place { } arcTo transition { }
        }
        assertTrue(errors.isNotEmpty()) { "errors were expected " }
        assertNotNull(errors.find { it is ConsistencyCheckError.IsolatedSubgraphsDetected }) { "isolated error expected" }
    }

    @Test
    fun checkMissingArcsAndTransitions() {
        val errors = createAndCheckForConsistency {
            place { }
            transition { }
            place { }
            place { } arcTo transition { } arcTo place { }
        }

        assertNotNull(errors.find { it is ConsistencyCheckError.MissingArc })
        assertNotNull(errors.find { it is ConsistencyCheckError.IsolatedPlace })
        // can test missing arc but can't test if arc misses something - this dsl doesn't allow this
    }

    @Test
    fun checkIsConsistentWhenWarningsAndOnlyVariableArcs() {
        // TODO: check that input and output are presented for a subgraph
        val ocScope = OCNetBuilder.define {
            place { }
            place { }
            place {
                placeType = PlaceType.INPUT
            }
                .variableArcTo(transition { })
                .arcTo(place {
                    placeType = PlaceType.OUTPUT
                    objectType = objectType("custom") { "custom_$it" }
                })
        }
        val converter = OCNetDSLConverter(ocScope)
        val convertionResult = converter.convert()
        val ocnetChecker = OCNetChecker(convertionResult.allPetriNodes)
        val errors = ocnetChecker.checkConsistency()

        assertTrue(ocnetChecker.isConsistent) {
            "expected consistent, errors:\n${errors.joinToString(separator = "\n").prependIndent()}"
        }
        assertEquals(2, ocScope.getFilteredObjectTypes().size) {
            "expected size 2: 2 object types were used (default, and custom)"
        }
        assertNotNull(errors.find { it is ConsistencyCheckError.VariableArcIsTheOnlyConnected } )
    }

    @Test
    fun testIsNotBipartiteError() {

    }

    @Test
    fun testOutputPlaceWithOutputArcsError() {

    }

    @Test
    fun testInputPlaceWithInputArcsError() {

    }

    @Test
    fun testMultipleArcsFromSinglePlaceError() {

    }

    @Test
    fun testArcInputAsArcOutputError() {

    }

    @Test
    fun testInconsistentVariabilityArcs() {

    }

    // TODO: create tests for consistency error cases (incorrect model)
}
