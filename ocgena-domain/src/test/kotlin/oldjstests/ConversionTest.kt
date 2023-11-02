package oldjstests

import org.junit.jupiter.api.Test

class ConversionTest {

    @Test
    fun testConfigConvertion() {
//        val simulationConfig = SimulationConfig(
//            arrayOf(
//                InputPlacesConfig("p1 p2 p3 p4"),
//                OutputPlacesConfig("p5 p6 p7"),
//                PlaceTypingConfig.fastCreate("ot1 = p1 p2 p5; ot2 = p3 p6; ot3 = p4 p7;")
//            )
//        )
//        val configToDomainConverter = SimulationConfigProcessor(
//            simulationConfig
//        )
//
//        val processResult = configToDomainConverter.createProcessedConfig()
//
//        val inputOutputPlaces = processResult.inputOutputPlaces
//
//        val placetyping = processResult.placeTyping
//
//        assertEquals(inputOutputPlaces["p1"], PlaceType.INPUT)
//        assertEquals(inputOutputPlaces["p2"], PlaceType.INPUT)
//        assertEquals(inputOutputPlaces["p3"], PlaceType.INPUT)
//        assertEquals(inputOutputPlaces["p4"], PlaceType.INPUT)
//
//        assertEquals(inputOutputPlaces["p5"], PlaceType.OUTPUT)
//        assertEquals(inputOutputPlaces["p6"], PlaceType.OUTPUT)
//        assertEquals(inputOutputPlaces["p7"], PlaceType.OUTPUT)
//
//        assertEquals(placetyping["p1"].id, "ot")
//        assertEquals(placetyping["p3"].id, "ot")
//        assertEquals(placetyping["p4"].id, "ot")
    }

    @Test
    fun checkModelIsBuilt() {
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
    }

    @Test
    fun checkSimulation() {

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
    }
}
