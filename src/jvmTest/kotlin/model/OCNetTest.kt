package model

import dsl.OCNetFacadeBuilder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OCNetTest {
    @Test
    fun testRunSimpleModel() {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val ocNet = ocNetFacadeBuilder.tryBuildModel {
            place {
                initialTokens = 4
                placeType = PlaceType.INPUT
            }
                .arcTo(transition {  })
                .arcTo(place { placeType = PlaceType.OUTPUT })
        }
        assertNotNull(ocNet) {
            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
        }
        requireNotNull(ocNet)
        runBlocking {
            ocNet.run(
                executionConditions = OCNet.ConsoleDebugExecutionConditions(),
                logger = OCNet.DebugLogger()
            )
        }

    }
}
