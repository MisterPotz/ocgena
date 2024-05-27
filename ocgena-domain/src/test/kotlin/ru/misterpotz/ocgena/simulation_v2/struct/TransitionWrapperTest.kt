package ru.misterpotz.ocgena.simulation_v2.struct

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.input.SynchronizedArcGroup
import ru.misterpotz.ocgena.simulation_v2.input.TransitionSetting
import ru.misterpotz.ocgena.simulation_v2.utils.toDefaultSim
import ru.misterpotz.ocgena.testing.buildOCNet

class TransitionWrapperTest {

    private val leftSync = "leftsync"
    private val middleSync = "middlesync"
    private val rightSync = "rightsync"

    private fun ocnet(): OCNetStruct {
        return buildOCNet {
            "i1".p { input; }
            "i3".p { input }

            val leftSync = leftSync.t()
            val middleSync = middleSync.t()
            val rightSync = rightSync.t()

            "i1".arc(leftSync.t()) { multiplicity = 2 }

            "i1".arc(middleSync)
            "i3".arc(middleSync.t())
            "i3".arc(rightSync.t())

            middleSync.arc("i1")
            middleSync.arc("i3")

            leftSync.arc("p1".p)
            leftSync.arc("p3".p)
            middleSync.arc("p2".p { objectTypeId = "2" })
            rightSync.arc("p3".p())

            val t = "testt".t()

            "p1".p.arc(t) { norm; }
            "p2".arc(t) { vari; }
            "p3".arc(t) { norm; multiplicity = 2 }

            t.arc("out".p { output }) {
                norm
            }
        }
    }

    @Test
    fun print() {
        println(ocnet().toDot())
    }

    @Test
    fun testSimple() {
        val transition = ocnet().toDefaultSim().transitionBy("testt")

        Assertions.assertEquals(
            listOf(transition.inputArcBy("p1"), transition.inputArcBy("p2"), transition.inputArcBy("p3")),
            transition.inputArcs
        )
    }

    @Test
    fun `1 group with common dependencies`() {
        val simulationInput = SimulationInput(
            transitions = buildMap {
                put("testt", TransitionSetting(
                    synchronizedArcGroups = buildList {
                        add(SynchronizedArcGroup(middleSync, arcsFromPlaces = listOf("p1", "p2")))
                        add(SynchronizedArcGroup(leftSync, arcsFromPlaces = listOf("p1", "p3")))
                    }
                ))
            }
        )

        val transition = ocnet().toDefaultSim(simulationInput).transitionBy("testt")

        Assertions.assertEquals(
            listOf(transition.inputArcBy("p1"), transition.inputArcBy("p2"), transition.inputArcBy("p3")),
            transition.inputArcs
        )

        Assertions.assertEquals(1, transition.independentMultiArcConditions.size)
        Assertions.assertTrue(transition.independentMultiArcConditions.first().conditions.isNotEmpty())
    }

    @Test
    fun `2 groups with common dependencies`() {
        val simulationInput = SimulationInput(
            transitions = buildMap {
                put("testt", TransitionSetting(
                    synchronizedArcGroups = buildList {
                        add(SynchronizedArcGroup(middleSync, arcsFromPlaces = listOf("p1", "p2")))
                        add(SynchronizedArcGroup(leftSync, arcsFromPlaces = listOf("p1")))
                        add(SynchronizedArcGroup(rightSync, arcsFromPlaces = listOf("p3")))
                    }
                ))
            }
        )

        val transition = ocnet().toDefaultSim(simulationInput).transitionBy("testt")

        Assertions.assertEquals(
            listOf(transition.inputArcBy("p1"), transition.inputArcBy("p2"), transition.inputArcBy("p3")),
            transition.inputArcs
        )

        Assertions.assertEquals(2, transition.independentMultiArcConditions.size)

        Assertions.assertEquals(
            simulationInput.transitions["testt"]!!.synchronizedArcGroups!!.let { list ->
                buildSet {
                    buildSet {
                        add(list[0])
                        add(list[1])
                    }.let(::add)

                    buildSet {
                        add(list[2])
                    }.let(::add)
                }
            },
            transition.independentMultiArcConditions.map {
                it.conditions.map { it.originalCondition }.toSet()
            }.toSet()
        )
    }
}