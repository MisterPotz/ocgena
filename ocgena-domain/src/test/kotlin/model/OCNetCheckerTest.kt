package model

import dsl.*
import ru.misterpotz.ocgena.error.ConsistencyCheckError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder
import ru.misterpotz.ocgena.simulation.logging.fastConsistencyDevSetup
import ru.misterpotz.ocgena.utils.findInstance
import ru.misterpotz.ocgena.validation.OCNetChecker

class OCNetCheckerTest {
    val ocNet = createExampleModel()

    fun createChecker(ocnet: OCNet) : OCNetChecker {
        return OCNetChecker(ocNet = ocnet, developmentDebugConfig = fastConsistencyDevSetup())
    }

    @Test
    fun checkConvertionResults() {
        assertTrue(ocNet.placeRegistry.isEmpty().not(), "converted places were empty ")

        assertEquals(14, ocNet.placeRegistry.iterable.toList().size, "incorrect amount of places")
        assertEquals(24, ocNet.arcsRegistry.iterable.toList().size, "incorrect amount of arcs")
        assertEquals(8, ocNet.transitionsRegistry.iterable.toList().size, "incorrect amount of transitions")
    }

    @Test
    fun checkCorrectModelIsConsistent() {
        val consistencyChecker = OCNetChecker(ocNet)
        val consistencyResults = consistencyChecker.checkConsistency()
        assertTrue(
            consistencyResults.isEmpty(),
            "inconsistencies detected:\n" + consistencyResults.joinToString(separator = "\n").prependIndent()
        )
        assertEquals(3, ocNet.inputPlaces.size)
        assertEquals(3, ocNet.outputPlaces.size)
        assertEquals(3, ocNet.objectTypeRegistry.size)
    }

    private fun createAndCheckForConsistency(
        block: OCNetBuilder.AtomDefinitionBlock.() -> Unit
    ): List<ConsistencyCheckError> {
        val ocNet = OCNetBuilder().defineAtoms(block)
        val ocnetChecker = OCNetChecker(
            ocNet,
        )
        return ocnetChecker.checkConsistency()
    }

    @Test
    fun `place types not specified`() {
        val errors = createAndCheckForConsistency {
            "p1".p
            "p2".p
                .arc("t1".t)
                .arc("p2".p)
        }
        assertTrue(errors.isNotEmpty(), "errors were expected ")
        assertNotNull(errors.find { it is ConsistencyCheckError.NoInputPlacesDetected }) { "isolated error expected" }
        assertNotNull(errors.find { it is ConsistencyCheckError.NoOutputPlacesDetected }) { "isolated error expected" }
    }

    @Test
    fun `isolated place`() {
        val errors = createAndCheckForConsistency {
            "p1".p
            "p2".p
                .arc("t1".t)
        }
        assertTrue(errors.isNotEmpty(), "errors were expected ")
        assertNotNull(errors.find { it is ConsistencyCheckError.IsolatedSubgraphsDetected }) { "isolated error expected" }
    }

    @Test
    fun checkMissingArcsAndTransitions() {
        val errors = createAndCheckForConsistency {
            "p1".p
            "t1".t
            "p2".p

            "p3".p()
                .arc("t2".t)
                .arc("p4".p)
        }

        assertNotNull(errors.find { it is ConsistencyCheckError.MissingArc })
        assertNotNull(errors.find { it is ConsistencyCheckError.IsolatedPlace })
        // can test missing arc but can't test if arc misses something - this dsl doesn't allow this
    }

    @Test
    fun checkIsConsistentWhenWarningsAndOnlyVariableArcs() {
        // TODO: check that input and output are presented for a subgraph
        val ocNet = OCNetBuilder().defineAtoms {
            "p1".p
            "p2".p { input }
                .arc("t1".t) { vari }
                .arc("p3".p { output }) { vari }
        }
        val ocnetChecker = OCNetChecker(ocNet)
        val errors = ocnetChecker.checkConsistency()

        assertTrue(
            ocnetChecker.isConsistent,
            "expected consistent, errors:\n${errors.joinToString(separator = "\n").prependIndent()}"
        )
        assertNotNull(errors.find { it is ConsistencyCheckError.VariableArcIsTheOnlyConnected })
    }


    @Test
    fun testIsNotBipartiteError() {
        val errors = createAndCheckForConsistency {
            "p1".p
                .arc("p2".p)
        }

        assertNotNull(errors.find { it is ConsistencyCheckError.IsNotBipartite })
    }

    @Test
    fun testOutputPlaceWithOutputArcsError() {
        val errors = createAndCheckForConsistency {
            "p1".p { output }
                .arc("t1".t)
                .arc("p2".p)
                .arc("t2".t)
                .arc("p3".p)
        }

        assertNotNull(errors.find { it is ConsistencyCheckError.OutputPlaceHasOutputArcs })
    }

    @Test
    fun testInputPlaceWithInputArcsError() {
        val errors = createAndCheckForConsistency {
            "p1".p
                .arc("p2".p { input })
        }

        assertNotNull(errors.find { it is ConsistencyCheckError.InputPlaceHasInputArcs })
    }

    @Test
    // due to map-like storage and how arc ids are constructed,
    // that's uncheckable - to check there are no arc duplicates - the arc between same nodes is always one
    fun testMultipleArcsFromSinglePlaceError() {
        val errors = createAndCheckForConsistency {
            val place = "p1".p
            "p2".p
                .arc("t1".t)
                .arc(place)

            "t1".t
                .arc(place)
        }
        assertNotNull(errors.findInstance<ConsistencyCheckError.NoInputPlacesDetected>())
        assertNotNull(errors.findInstance<ConsistencyCheckError.NoOutputPlacesDetected>())
    }

    @Test
    fun testArcInputAsArcOutputError() {
        val errors = createAndCheckForConsistency {
            val place = "p1".p
            place
                .arc(place)
        }
        assertNotNull(errors.find { it is ConsistencyCheckError.ArcInputEqualsOutput })
    }

    @Test
    fun testInconsistentVariabilityArcs() {
        val errors = createAndCheckForConsistency {
            "p1".p
                .arc("t1".t) { vari }
                .arc("p2".p)
        }
        assertNotNull(errors.find { it is ConsistencyCheckError.InconsistentVariabilityArcs })
    }
}
