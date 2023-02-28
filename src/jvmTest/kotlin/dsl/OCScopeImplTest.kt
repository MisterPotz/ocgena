package dsl

import model.PlaceType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * @see <img src="src/jvmTest/resources/img.png" >
 */
class OCScopeImplTest {

    private var _ocScopeImpl: OCScopeImpl? = null
    val ocScopeImpl
        get() = _ocScopeImpl!!

    init {
        _ocScopeImpl = OCScopeImpl(
            rootScope = null,
            defaultScopeType = null
        )
        ocScopeImpl.apply {
            val order = objectType("order") {
                "o$it"
            }
            val item = objectType("item")
            val route = objectType("route") {
                "r$it"
            }

            forType(order) {
                place {
                    placeType = PlaceType.INPUT
                }.arcTo(transition("place order"))
                    .arcTo(place { })
                    .arcTo(transition("send invoice"))
                    .arcTo(subgraph {
                        setAsInputOutput(place { })

                        input.arcTo(transition("send reminder"))
                        transition("send reminder").arcTo(input)
                    })
                    .arcTo(transition("pay order"))
                    .arcTo(place { })
                    .arcTo(transition("mark as completed"))
                    .arcTo(place {
                        placeType = PlaceType.OUTPUT
                    })
            }

            forType(item) {
                place {
                    placeType = PlaceType.INPUT
                }
                    .variableArcTo(transition("place order"))
                    .variableArcTo(place { })
                    .arcTo(transition("pick item"))
                    .arcTo(place { })
                    .variableArcTo(transition("start route"))
                    .variableArcTo(place { })
                    .variableArcTo(transition("end route"))
                    .variableArcTo(place { })
                    .variableArcTo(transition("mark as completed"))
                    .variableArcTo(place {
                        placeType = PlaceType.OUTPUT
                    })
            }

            forType(route) {
                place {
                    placeType = PlaceType.INPUT
                }.arcTo(transition("start route"))
                    .arcTo(place { })
                    .arcTo(transition("end route"))
                    .arcTo(place {
                        placeType = PlaceType.OUTPUT
                    })
            }
        }
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
        assertEquals(ocScopeImpl.objectTypes.size, 3)
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

            assertThrows<Throwable>{ inputArcFor(place("o1")) }
            inputArcFor(place("o2"))
            inputArcFor(place("o3"))
            inputArcFor(place("o4"))
            inputArcFor(place("o5"))


            outputArcFor(transition("place order"))
            outputArcFor(transition("send invoice"))
            outputArcFor(transition("send reminder"))
            outputArcFor(transition("pay order"))
            outputArcFor(transition("mark as completed"))

            inputArcFor(transition("place order"))
            inputArcFor(transition("send invoice"))
            inputArcFor(transition("send reminder"))
            inputArcFor(transition("pay order"))
            inputArcFor(transition("mark as completed"))
        }
    }
}
