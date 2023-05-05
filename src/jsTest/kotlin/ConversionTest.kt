import config.*
import converter.FullModelBuilder
import kotlinx.js.jso
import model.OcNetType
import model.PlaceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConversionTest {

    @Test
    fun testConfigConvertion() {
        val simulationConfig = SimulationConfig(
            arrayOf(
                InputPlacesConfig("p1 p2 p3 p4"),
                OutputPlacesConfig("p5 p6 p7"),
                PlaceTypingConfig(
                    jso {
                        ot1 = "p1 p2 p5"
                        ot2 = "p3 p6"
                        ot3 = "p4 p7"
                    }
                )
            )
        )
        val configToDomainConverter = ConfigToDomainConverter(
            simulationConfig
        )

        val processResult = configToDomainConverter.processAll()

        val inputOutputPlaces = processResult.inputOutputPlaces

        val placetyping = processResult.placeTyping

        assertEquals(inputOutputPlaces["p1"], PlaceType.INPUT)
        assertEquals(inputOutputPlaces["p2"], PlaceType.INPUT)
        assertEquals(inputOutputPlaces["p3"], PlaceType.INPUT)
        assertEquals(inputOutputPlaces["p4"], PlaceType.INPUT)

        assertEquals(inputOutputPlaces["p5"], PlaceType.OUTPUT)
        assertEquals(inputOutputPlaces["p6"], PlaceType.OUTPUT)
        assertEquals(inputOutputPlaces["p7"], PlaceType.OUTPUT)

        assertEquals(placetyping["p1"].id, "ot1")
        assertEquals(placetyping["p3"].id, "ot2")
        assertEquals(placetyping["p4"].id, "ot3")
    }

    @Test
    fun checkModelIsBuilt() {
        val simulationConfig = SimulationConfig(
            arrayOf(
                InputPlacesConfig("p1"),
                OutputPlacesConfig("p3"),
                PlaceTypingConfig(
                    jso {
                        ot1 = "p1 p2 p3"
                    }
                ),
                OCNetTypeConfig(OcNetType.TYPE_A.ordinal)
            )
        )
        val ocDot = """ocnet {
            |   places { 
            |       p1 p2 p3
            |   }
            |   transitions {
            |       t1 t2
            |   }
            |   p1 -> t1 -> p2 -> t2 -> p3
            |}
        """.trimMargin()

        val fullModelBuilder = FullModelBuilder()
        fullModelBuilder.with(ocDot)
        fullModelBuilder.with(simulationConfig)

        val processingResult = fullModelBuilder.tryBuildTask()!!.process()
        assertNotNull(processingResult.ocNet)
        println(processingResult)
    }
}
