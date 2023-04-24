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

            place {
                placeType = PlaceType.INPUT
            }.arcTo(transition("t1"))

            place {
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
//        val simulatorCreator =
//            SimulationCreator(ocNet, ConsoleDebugExecutionConditions(), DebugLogger(), SimulationParams(
//                initialMarking = PlainMarking.of {
//                    put("p1", 2)
//                    put("p2", 4)
//                }
//            )
//            )
//        simulatorCreator.createSimulationTask().prepareAndRun()
    }
}
