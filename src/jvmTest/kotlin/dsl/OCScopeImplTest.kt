package dsl

import model.PlaceType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class OCScopeImplTest {

    private var _ocScopeImpl: OCScopeImpl? = null
    val ocScopeImpl
        get() = _ocScopeImpl!!

    init {
        _ocScopeImpl = createExampleModel()
    }

    @Test
    fun testPlacesPerTypeAmount() {
        val order = ocScopeImpl.objectType("order")
        val item = ocScopeImpl.objectType("item")
        val route = ocScopeImpl.objectType("route")

        val orderPlaces = ocScopeImpl.places
            .values
            .filter { it.objectType == order }
        val itemPlaces = ocScopeImpl.places
            .values
            .filter { it.objectType == item }
        val routePlaces = ocScopeImpl.places
            .values
            .filter { it.objectType == route }

        assertEquals(5, orderPlaces.size)
        assertEquals(6, itemPlaces.size)
        assertEquals(3, routePlaces.size)
    }

    @Test
    fun testTransitionPresence() {
        ocScopeImpl.apply {
            assertNotNull(
                transitions.values.find { it.label == "place order" }
            )
            assertNotNull(
                transitions.values.find { it.label == "send invoice" }
            )
            assertNotNull(
                transitions.values.find { it.label == "send reminder" }
            )
            assertNotNull(
                transitions.values.find { it.label == "pay order" }
            )
            assertNotNull(
                transitions.values.find { it.label == "mark as completed" }
            )
            assertNotNull(
                transitions.values.find { it.label == "pick item" }
            )
            assertNotNull(
                transitions.values.find { it.label == "start route" }
            )
            assertNotNull(
                transitions.values.find { it.label == "end route" }
            )
            assertEquals(8, transitions.size)
        }
    }

    @Test
    fun testObjectTypesAmount() {
        assertEquals(ocScopeImpl.getFilteredObjectTypes().size, 3)
    }

    @Test
    fun testPlaceTypes() {
        assertEquals(
            3,
            ocScopeImpl.places.values.filter { it.placeType == PlaceType.INPUT }.size
        )
        assertEquals(
            3,
            ocScopeImpl.places.values.filter { it.placeType == PlaceType.OUTPUT }.size
        )
        assertEquals(
            3,
            ocScopeImpl.places.values.filter { it.placeType == PlaceType.OUTPUT }.size
        )
        // totally 14 places
        assertEquals(14, ocScopeImpl.places.size)
    }

    @Test
    fun testArcsAmount() {
        assertEquals(24, ocScopeImpl.arcs.size)

        assertEquals(8, ocScopeImpl.arcs.filterIsInstance<VariableArcDSL>().size)

        assertEquals(16, ocScopeImpl.arcs.filterIsInstance<NormalArcDSL>().size)
    }

    @Test
    fun testPlaceNaming() {
        val indexRegex = Regex("""\d+""")
        ocScopeImpl.places.values
            .filter {
                it.label.matches(Regex("""o\d+"""))
            }.let {
                assertEquals(5, it.size)
                assertTrue(it.all {
                    val match = indexRegex.find(it.label)
                    match != null && match.groupValues.first().toInt() in 1..5
                })
            }
        ocScopeImpl.places.values
            .filter {
                it.label.matches(Regex("""item_\d+"""))
            }.let {
                assertEquals(6, it.size)
                assertTrue(it.all {
                    val match = indexRegex.find(it.label)
                    match != null && match.groupValues.first().toInt() in 1..6
                })
            }
        ocScopeImpl.places.values
            .filter {
                it.label.matches(Regex("""r\d+"""))
            }.let {
                assertEquals(3, it.size)
                assertTrue(it.all {
                    val match = indexRegex.find(it.label)
                    match != null && match.groupValues.first().toInt() in 1..3
                })
            }
    }

    @Test
    fun testAccessToNodesAndConnectedArcs() {
        with(ocScopeImpl) {
            arcs.forEach {
                println(it)
            }
            outputArcFor(place("o1"))
            outputArcFor(place("o2"))
            outputArcFor(place("o3"))
            outputArcFor(place("o4"))
            assertThrows<Throwable>{ outputArcFor(place("o5")) }

            assertEquals(2, outputArcsFor(place("o3")).size)
            assertEquals(2, inputArcsFor(place("o3")).size)


            assertThrows<Throwable>{ inputArcFor(place("o1")) }
            inputArcFor(place("o2"))
            inputArcFor(place("o3"))
            inputArcFor(place("o4"))
            inputArcFor(place("o5"))
        }
    }

    @Test
    fun testTransitionArcs() {
        with(ocScopeImpl) {
            // output
            outputArcsFor(transition("place order")).let {
                assertEquals(2, it.size)
                assertNotNull(it.find { it is VariableArcDSL })
                assertNotNull(it.find { it is NormalArcDSL })
            }
            outputArcsFor(transition("send invoice")).let {
                assertEquals(1, it.size)
                assertTrue(it.first() is NormalArcDSL)
            }
            outputArcsFor(transition("send reminder")).let {
                assertEquals(1, it.size)
            }
            outputArcsFor(transition("pay order")).let {
                assertEquals(1, it.size)
            }
            outputArcsFor(transition("mark as completed")).let {
                assertEquals(2, it.size)
                assertNotNull(it.find { it is VariableArcDSL })
                assertNotNull(it.find { it is NormalArcDSL })
            }
            outputArcsFor(transition("pick item")).let {
                assertEquals(1, it.size)
            }
            outputArcsFor(transition("start route")).let {
                assertEquals(2, it.size)
                assertNotNull(it.find { it is VariableArcDSL })
                assertNotNull(it.find { it is NormalArcDSL })
            }
            outputArcsFor(transition("end route")).let {
                assertEquals(2, it.size)
                assertNotNull(it.find { it is VariableArcDSL })
                assertNotNull(it.find { it is NormalArcDSL })
            }

            // input
            inputArcsFor(transition("place order")).let {
                assertEquals(2, it.size)
                assertNotNull(it.find { it is VariableArcDSL })
                assertNotNull(it.find { it is NormalArcDSL })
            }
            inputArcsFor(transition("send invoice")).let {
                assertEquals(1, it.size)
                assertTrue(it.first() is NormalArcDSL)
            }
            inputArcsFor(transition("send reminder")).let {
                assertEquals(1, it.size)
            }
            inputArcsFor(transition("pay order")).let {
                assertEquals(1, it.size)
            }
            inputArcsFor(transition("mark as completed")).let {
                assertEquals(2, it.size)
                assertNotNull(it.find { it is VariableArcDSL })
                assertNotNull(it.find { it is NormalArcDSL })
            }
            inputArcsFor(transition("pick item")).let {
                assertEquals(1, it.size)
            }
            inputArcsFor(transition("start route")).let {
                assertEquals(2, it.size)
                assertNotNull(it.find { it is VariableArcDSL })
                assertNotNull(it.find { it is NormalArcDSL })
            }
            inputArcsFor(transition("end route")).let {
                assertEquals(2, it.size)
                assertNotNull(it.find { it is VariableArcDSL })
                assertNotNull(it.find { it is NormalArcDSL })
            }
        }
    }
}
