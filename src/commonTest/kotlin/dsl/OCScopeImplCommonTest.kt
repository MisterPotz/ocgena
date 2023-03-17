package dsl

import model.PlaceType
import utils.mprintln
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class OCScopeImplCommonTest {

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

        val orderPlaces = ocScopeImpl.totalPlaces
            .values
            .filter { it.objectType == order }
        val itemPlaces = ocScopeImpl.totalPlaces
            .values
            .filter { it.objectType == item }
        val routePlaces = ocScopeImpl.totalPlaces
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
                totalTransitions.values.find { it.label == "place order" }
            )
            assertNotNull(
                totalTransitions.values.find { it.label == "send invoice" }
            )
            assertNotNull(
                totalTransitions.values.find { it.label == "send reminder" }
            )
            assertNotNull(
                totalTransitions.values.find { it.label == "pay order" }
            )
            assertNotNull(
                totalTransitions.values.find { it.label == "mark as completed" }
            )
            assertNotNull(
                totalTransitions.values.find { it.label == "pick item" }
            )
            assertNotNull(
                totalTransitions.values.find { it.label == "start route" }
            )
            assertNotNull(
                totalTransitions.values.find { it.label == "end route" }
            )
            assertEquals(8, totalTransitions.size)
        }
    }

    @Test
    fun testObjectTypesAmount() {
        assertEquals(ocScopeImpl.getFilteredObjectTypes().size, 3)
    }

    @Test
    fun testTransitionsAmount() {
        assertEquals(ocScopeImpl.totalTransitions.size, 8)
    }

    @Test
    fun testPlaceTypes() {
        assertEquals(
            3,
            ocScopeImpl.totalPlaces.values.filter { it.placeType == PlaceType.INPUT }.size
        )
        assertEquals(
            3,
            ocScopeImpl.totalPlaces.values.filter { it.placeType == PlaceType.OUTPUT }.size
        )
        assertEquals(
            3,
            ocScopeImpl.totalPlaces.values.filter { it.placeType == PlaceType.OUTPUT }.size
        )
        // totally 14 places
        assertEquals(14, ocScopeImpl.totalPlaces.size)
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
        ocScopeImpl.totalPlaces.values
            .filter {
                it.label.matches(Regex("""o\d+"""))
            }.let {
                assertEquals(5, it.size)
                assertTrue(it.all {
                    val match = indexRegex.find(it.label)
                    match != null && match.groupValues.first().toInt() in 1..5
                })
            }
        ocScopeImpl.totalPlaces.values
            .filter {
                it.label.matches(Regex("""item_\d+"""))
            }.let {
                assertEquals(6, it.size)
                assertTrue(it.all {
                    val match = indexRegex.find(it.label)
                    match != null && match.groupValues.first().toInt() in 1..6
                })
            }
        ocScopeImpl.totalPlaces.values
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
        val arcSearcher = ArcSearcher(ocScopeImpl)
        with(arcSearcher) {
            with(ocScopeImpl) {
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
        val arcSearcher = ArcSearcher(ocScopeImpl)

        with(arcSearcher) {
            with(ocScopeImpl) {
                arcs.forEach {
                    mprintln(it)
                }
                outputArcFor(place("item_1"))
                outputArcFor(place("item_2"))
                outputArcFor(place("item_3"))
                outputArcFor(place("item_4"))
                outputArcFor(place("item_5"))


                inputArcFor(place("item_2"))
                inputArcFor(place("item_3"))
                inputArcFor(place("item_4"))
                inputArcFor(place("item_5"))
                inputArcFor(place("item_6"))
            }
        }
    }

    @Test
    fun testTransitionArcs() {
        val arcSearcher = ArcSearcher(ocScopeImpl)
        with(arcSearcher) {
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
}
