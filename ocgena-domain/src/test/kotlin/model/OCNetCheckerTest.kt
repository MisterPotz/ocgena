package model

import dsl.*
import error.ConsistencyCheckError
import model.utils.OCNetDSLConverter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.registries.PlaceObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry
import ru.misterpotz.ocgena.validation.OCNetChecker

class OCNetCheckerTest {
    val ocScopeImpl = createExampleModel()
    private val placeTyping = createExamplePlaceTyping()
    private val inputOutputPlaces = createExampleInputOutputPlaces()

    @Test
    fun checkConvertionHappensAtAll() {
        val converter = OCNetDSLConverter(ocScopeImpl, placeTyping)
        val result = converter.convert()
    }

    @Test
    fun checkConvertionResults() {
        val converter = OCNetDSLConverter(ocScopeImpl, placeTyping)
        val result = converter.convert()

        assertTrue(result.places.isNotEmpty(), "converted places were empty ")

        assertTrue(result.places.size == 14, "incorrect amount of places")
//        assertEquals(24, result.arcs.size, "incorrect amount of arcs")
        assertEquals(8, result.transitions.size, "incorrect amount of transitions")
    }

    @Test
    fun checkCorrectModelIsConsistent() {
        val converter = OCNetDSLConverter(ocScopeImpl, placeTyping)
        val result = converter.convert()
        val places = result.places
        val consistencyChecker = OCNetChecker(
            result,
            placeTyping,
            inputOutputPlaces
        )
        val consistencyResults = consistencyChecker.checkConsistency()
        assertTrue(
            consistencyResults.isEmpty(),
            "inconsistencies detected:\n" + consistencyResults.joinToString(separator = "\n").prependIndent()
        )
        val consistentNet = consistencyChecker.createWellFormedOCNet()
        assertEquals(3, consistentNet.inputPlaces.size)
        assertEquals(3, consistentNet.outputPlaces.size)
        assertEquals(3, consistentNet.objectTypes.size)
    }

    private fun createAndCheckForConsistency(
        placeTypeRegistry: PlaceTypeRegistry,
        placeObjectTypeRegistry: PlaceObjectTypeRegistry = PlaceObjectTypeRegistry.build(),
        block: OCScope.() -> Unit
    ): List<ConsistencyCheckError> {
        val ocScope = OCNetBuilder.define(block)
        val converter = OCNetDSLConverter(ocScope, placeObjectTypeRegistry)
        val convertionResult = converter.convert()
        val ocnetChecker = OCNetChecker(
            convertionResult,
            placeObjectTypeRegistry,
            placeTypeRegistry
        )
        return ocnetChecker.checkConsistency()
    }

    @Test
    fun checkInputOutputAbsence() {
        val errors = createAndCheckForConsistency(
            placeTypeRegistry = PlaceTypeRegistry.build {
            },
            placeObjectTypeRegistry = PlaceObjectTypeRegistry.build()
        ) {
            place { }
            // place types not specified
            place { }.arcTo(transition { }).arcTo(place { })
        }
        assertTrue(errors.isNotEmpty(), "errors were expected ")
        assertNotNull(errors.find { it is ConsistencyCheckError.NoInputPlacesDetected }) { "isolated error expected" }
        assertNotNull(errors.find { it is ConsistencyCheckError.NoOutputPlacesDetected }) { "isolated error expected" }
    }

    @Test
    fun checkIsolatedSubgraphPresent() {
        val errors = createAndCheckForConsistency(
            placeTypeRegistry = PlaceTypeRegistry.build { }
        ) {
            // isolated place
            place { }
            place { }.arcTo(transition { })
        }
        assertTrue(errors.isNotEmpty(), "errors were expected ")
        assertNotNull(errors.find { it is ConsistencyCheckError.IsolatedSubgraphsDetected }) { "isolated error expected" }
    }

    @Test
    fun checkMissingArcsAndTransitions() {
        val errors = createAndCheckForConsistency(
            PlaceTypeRegistry.build { }
        ) {
            place { }
            transition { }
            place { }
            place { }.arcTo(transition { }).arcTo(place { })
        }

        assertNotNull(errors.find { it is ConsistencyCheckError.MissingArc })
        assertNotNull(errors.find { it is ConsistencyCheckError.IsolatedPlace })
        // can test missing arc but can't test if arc misses something - this dsl doesn't allow this
    }

    @Test
    fun checkIsConsistentWhenWarningsAndOnlyVariableArcs() {
        // TODO: check that input and output are presented for a subgraph

        val placeObjectTypeRegistry = PlaceObjectTypeRegistry.build {
            objectType("custom", "p4")
        }
        val placeTypeRegistry = PlaceTypeRegistry.build {
            inputPlaces("p3")
            outputPlaces("p4")
        }
        val ocScope = OCNetBuilder.define {
            place { }
            place { }
            place { }
                .variableArcTo(transition { })
                .arcTo(place { })
        }
        val converter = OCNetDSLConverter(ocScope, placeObjectTypeRegistry)
        val convertionResult = converter.convert()
        val ocnetChecker = OCNetChecker(convertionResult, placeObjectTypeRegistry, placeTypeRegistry)
        val errors = ocnetChecker.checkConsistency()
        val objectSearcher = ObjectsSearcher(ocScope)

        assertTrue(
            ocnetChecker.isConsistent,
            "expected consistent, errors:\n${errors.joinToString(separator = "\n").prependIndent()}"
        )
        assertNotNull(errors.find { it is ConsistencyCheckError.VariableArcIsTheOnlyConnected })
    }


    @Test
    fun testIsNotBipartiteError() {
        val errors = createAndCheckForConsistency(
            PlaceTypeRegistry.build { }
        ) {
            place { }.arcTo(place { })
        }

        assertNotNull(errors.find { it is ConsistencyCheckError.IsNotBipartite })
    }

    @Test
    fun testOutputPlaceWithOutputArcsError() {
        val errors = createAndCheckForConsistency(
            PlaceTypeRegistry.build {
                inputPlaces("p1")
                outputPlaces("p2 p3")
            }
        ) {
            place { }
                .arcTo(transition { })
                .arcTo(place { })
                .arcTo(transition { })
                .arcTo(place { })
        }

        assertNotNull(errors.find { it is ConsistencyCheckError.OutputPlaceHasOutputArcs })
    }

    @Test
    fun testInputPlaceWithInputArcsError() {
        val errors = createAndCheckForConsistency(
            PlaceTypeRegistry.build {
                inputPlaces("p2")
            }
        ) {
            place { }
                .arcTo(place { })
        }

        assertNotNull(errors.find { it is ConsistencyCheckError.InputPlaceHasInputArcs })
    }

    @Test
    fun testMultipleArcsFromSinglePlaceError() {
        val errors = createAndCheckForConsistency(
            PlaceTypeRegistry.build {

            }
        ) {
            val place = place { }
            place { }
                .arcTo(transition("t"))
                .arcTo(place)

            transition("t")
                .arcTo(place)
        }
        assertNotNull(errors.find { it is ConsistencyCheckError.MultipleArcsFromSinglePlaceToSingleTransition })
    }

    @Test
    fun testArcInputAsArcOutputError() {
        val errors = createAndCheckForConsistency(
            PlaceTypeRegistry.build { }
        ) {
            val place = place { }
            place
                .arcTo(place)
        }
        assertNotNull(errors.find { it is ConsistencyCheckError.ArcInputEqualsOutput })
    }

    @Test
    fun testInconsistentVariabilityArcs() {
        val errors = createAndCheckForConsistency(
            PlaceTypeRegistry.build { }
        ) {
            place { }
                .arcTo(transition { })
                .variableArcTo(place { })
        }
        assertNotNull(errors.find { it is ConsistencyCheckError.InconsistentVariabilityArcs })
    }
}
