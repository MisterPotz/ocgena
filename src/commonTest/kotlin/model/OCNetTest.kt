package model

import dsl.OCNetFacadeBuilder
import error.prettyPrint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class OCNetTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testRunSimpleModel() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val ocNet = ocNetFacadeBuilder.tryBuildModel {
            place {
                initialTokens = 4
                placeType = PlaceType.INPUT
            }
                .arcTo(transition { })
                .arcTo(place { placeType = PlaceType.OUTPUT })
        }.requireConsistentOCNet()
        assertNotNull(
            ocNet,
            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
        )
        requireNotNull(ocNet)
        ocNet.run(
            executionConditions = WellFormedOCNet.ConsoleDebugExecutionConditions(),
            logger = WellFormedOCNet.DebugLogger()
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAnotherModel() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val ocNet = ocNetFacadeBuilder.tryBuildModel {

            place {
                initialTokens = 2
                placeType = PlaceType.INPUT
            }.arcTo(transition("t1"))

            place {
                initialTokens = 4
                placeType = PlaceType.INPUT
            }
                .arcTo(transition("t1"))
                .arcTo(place { })
                .arcTo(transition { }) {
                    multiplicity = 2
                }
                .arcTo(place { placeType = PlaceType.OUTPUT }) {
                    multiplicity = 3
                }
        }.requireConsistentOCNet()
        assertNotNull(ocNet) {
            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
        }
        requireNotNull(ocNet)
        ocNet.run(
            executionConditions = WellFormedOCNet.ConsoleDebugExecutionConditions(),
            logger = WellFormedOCNet.DebugLogger()
        )
    }
}
