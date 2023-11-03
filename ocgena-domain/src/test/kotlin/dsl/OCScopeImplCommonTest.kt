package dsl

import model.PlaceType
import ru.misterpotz.ocgena.dsl.NormalArcDSL
import ru.misterpotz.ocgena.dsl.VariableArcDSL
import utils.mprintln
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class OCScopeImplCommonTest {

    private val ocNetDSLElements = createExampleModel()
    private val placeTyping = createExamplePlaceTyping()
    private val inputOutputPlaces = createExampleInputOutputPlaces()
//    private val objectSearcher = ObjectsSearcher(ocNetDSLElements)


    @Test
    fun testPlacesPerTypeAmount() {
        val order = ocNetDSLElements.objectType("order")
        val item = ocNetDSLElements.objectType("item")
        val route = ocNetDSLElements.objectType("route")

        val orderPlaces = ocNetDSLElements.places
            .values
            .filter { placeTyping[it.label].label == order.label }
        val itemPlaces = ocNetDSLElements.places
            .values
            .filter {  placeTyping[it.label].label == item.label }
        val routePlaces = ocNetDSLElements.places
            .values
            .filter {  placeTyping[it.label].label == route.label }

        assertEquals(5, orderPlaces.size)
        assertEquals(6, itemPlaces.size)
        assertEquals(3, routePlaces.size)
    }

    @Test
    fun testTransitionPresence() {
        ocNetDSLElements.apply {
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

//    @Test
//    fun testObjectTypesAmount() {
//        assertEquals(objectSearcher.withoutDefaultObjectTypeIfPossible().size, 3)
//    }

    @Test
    fun testTransitionsAmount() {
        assertEquals(ocNetDSLElements.transitions.size, 8)
    }

    @Test
    fun testPlaceTypes() {
        assertEquals(
            3,
            ocNetDSLElements.places.values.filter { inputOutputPlaces[it.label] == PlaceType.INPUT }.size
        )
        assertEquals(
            3,
            ocNetDSLElements.places.values.filter {  inputOutputPlaces[it.label] == PlaceType.OUTPUT }.size
        )
        assertEquals(
            3,
            ocNetDSLElements.places.values.filter {  inputOutputPlaces[it.label] == PlaceType.OUTPUT }.size
        )
        // totally 14 places
        assertEquals(14, ocNetDSLElements.places.size)
    }

    @Test
    fun testArcsAmount() {
        assertEquals(24, ocNetDSLElements.arcs.size)

        assertEquals(8, ocNetDSLElements.arcs.filterIsInstance<VariableArcDSL>().size)

        assertEquals(16, ocNetDSLElements.arcs.filterIsInstance<NormalArcDSL>().size)
    }

    @Test
    fun testPlaceNaming() {
        val indexRegex = Regex("""\d+""")
        ocNetDSLElements.places.values
            .filter {
                it.label.matches(Regex("""o\d+"""))
            }.let {
                assertEquals(5, it.size)
                assertTrue(it.all {
                    val match = indexRegex.find(it.label)
                    match != null && match.groupValues.first().toInt() in 1..5
                })
            }
        ocNetDSLElements.places.values
            .filter {
                it.label.matches(Regex("""i\d+"""))
            }.let {
                assertEquals(6, it.size)
                assertTrue(it.all {
                    val match = indexRegex.find(it.label)
                    match != null && match.groupValues.first().toInt() in 1..6
                })
            }
        ocNetDSLElements.places.values
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
    fun testAccessToNodesAndConnectedArcsForOrder() {
        val arcSearcher = ArcSearcher(ocNetDSLElements)
        with(arcSearcher) {
            with(ocNetDSLElements) {
                arcs.forEach {
                    mprintln(it)
                }
                outputArcFor(place("o1"))
                outputArcFor(place("o2"))
                outputArcFor(place("o3"))
                outputArcFor(place("o4"))
                assertFails { outputArcFor(place("o5")) }

                assertEquals(2, outputArcsFor(place("o3")).size)
                assertEquals(2, inputArcsFor(place("o3")).size)


                assertFails { inputArcFor(place("o1")) }
                inputArcFor(place("o2"))
                inputArcFor(place("o3"))
                inputArcFor(place("o4"))
                inputArcFor(place("o5"))
            }
        }
    }

    @Test
    fun testAccessToNodesAndConnectedArcsForItem() {
        val arcSearcher = ArcSearcher(ocNetDSLElements)

        with(arcSearcher) {
            with(ocNetDSLElements) {
                arcs.forEach {
                    mprintln(it)
                }
                outputArcFor(place("i1"))
                outputArcFor(place("i2"))
                outputArcFor(place("i3"))
                outputArcFor(place("i4"))
                outputArcFor(place("i5"))


                inputArcFor(place("i2"))
                inputArcFor(place("i3"))
                inputArcFor(place("i4"))
                inputArcFor(place("i5"))
                inputArcFor(place("i6"))
            }
        }
    }

    @Test
    fun testTransitionArcs() {
        val arcSearcher = ArcSearcher(ocNetDSLElements)
        with(arcSearcher) {
            with(ocNetDSLElements) {
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
}
