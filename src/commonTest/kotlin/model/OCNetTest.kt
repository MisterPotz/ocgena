package model

import dsl.OCNetFacadeBuilder
import error.prettyPrint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import model.aalst.SimulationParamsTypeABuilder
import simulation.ConsoleDebugExecutionConditions
import simulation.DebugLogger
import simulation.PlainMarking
import simulation.SimulationCreator
import kotlin.test.Test
import kotlin.test.assertNotNull

class OCNetTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test()
    fun testRunSimpleModel() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val ocNet = ocNetFacadeBuilder.tryBuildModel {
            place {
                placeType = PlaceType.INPUT
            }
                .arcTo(transition { })
                .arcTo(
                    place {
                        placeType = PlaceType.OUTPUT
                    }
                )
        }.requireConsistentOCNet()


        assertNotNull(
            ocNet,
            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
        )

        val places = ocNet.places
        val transitions = ocNet.transitions
        val simulationParamsTypeABuilder = SimulationParamsTypeABuilder(ocNet)
            .withInitialMarking(
                PlainMarking.of {
                    put(places["p1"], 10)
                }
            )
            .withTimeIntervals(
                IntervalFunction.create {
                    put(
                        transitions["t1"], FiringTimePair(
                            earlyFiringTime = 10,
                            latestFiringTime = 15
                        )
                    )
                }
            )

        val simulatorCreator = SimulationCreator(
            simulationParams = simulationParamsTypeABuilder.build(),
            executionConditions = ConsoleDebugExecutionConditions(),
            logger = DebugLogger()
        )
        simulatorCreator
            .createSimulationTask()
            .prepareAndRun()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAnotherModel() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val ocNet = ocNetFacadeBuilder.tryBuildModel {

            place("p1") {
                placeType = PlaceType.INPUT
            }.arcTo(transition("t1"))

            place("p2") {
                placeType = PlaceType.INPUT
            }
                .arcTo(transition("t1"))
                .arcTo(place("p3") { }) {
                    multiplicity = 2
                }
                .arcTo(transition { }) {
                    multiplicity = 2
                }
                .arcTo(place("p4") {
                    placeType = PlaceType.OUTPUT
                }) {
                    multiplicity = 3
                }
        }.requireConsistentOCNet()
        assertNotNull(ocNet) {
            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
        }
        requireNotNull(ocNet)

        val places = ocNet.places
        val transitions = ocNet.transitions
        val simulationParamsTypeABuilder = SimulationParamsTypeABuilder(ocNet)
            .withInitialMarking(
                PlainMarking.of {
                    put(places["p1"], 10)
                    put(places["p2"], 4)
                }
            )
            .withTimeIntervals(
                IntervalFunction.create {
                    put(
                        transitions["t1"], FiringTimePair(
                            earlyFiringTime = 10,
                            latestFiringTime = 15
                        )
                    )
                    put(transitions["t2"], FiringTimePair(earlyFiringTime = 0, latestFiringTime = 5))
                }
            )

        val simulatorCreator = SimulationCreator(
            simulationParams = simulationParamsTypeABuilder.build(),
            executionConditions = ConsoleDebugExecutionConditions(),
            logger = DebugLogger()
        )
        simulatorCreator
            .createSimulationTask()
            .prepareAndRun()
    }
}
