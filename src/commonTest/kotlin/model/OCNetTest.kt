package model

import dsl.OCNetFacadeBuilder
import error.prettyPrint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import model.typea.SimulationParamsTypeABuilder
import model.time.IntervalFunction
import model.time.TransitionTimes
import simulation.*
import kotlin.test.Test
import kotlin.test.assertNotNull

class OCNetTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test()
    fun testRunSimpleModel() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val inputOutputPlaces = InputOutputPlaces.build {
            inputPlaces("p1")
            outputPlaces("p2")
        }
        val placeTyping = PlaceTyping.build()

        val ocNet = ocNetFacadeBuilder.tryBuildModel(
            placeTyping = placeTyping,
            inputOutputPlaces = inputOutputPlaces
        ) {
            place { }
                .arcTo(transition { })
                .arcTo(place { })
        }.requireConsistentOCNet()

        assertNotNull(
            ocNet,
            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
        )

        val places = ocNet.places
        val transitions = ocNet.transitions
        val simulationParamsTypeABuilder = SimulationParamsTypeABuilder(ocNet)
            .withPlaceTyping(PlaceTyping.build())
            .withInitialMarking(
                PlainMarking.of {
                    put(places["p1"], 10)
                }
            )
            .withInputOutput(
                InputOutputPlaces.build {
                    inputPlaces("p1")
                    outputPlaces("p2")
                }
            )
            .withTimeIntervals(
                IntervalFunction.create {
                    put(
                        transitions["t1"], TransitionTimes(
                            duration = 10..15,
                            pauseBeforeNextOccurence = 0..0
                        )
                    )
                }
            )

        val simulatorCreator = SimulationCreator(
            simulationParams = simulationParamsTypeABuilder.build(),
            executionConditions = ConsoleDebugExecutionConditions(),
            logger = LoggerFactoryDefault
        )
        simulatorCreator
            .createSimulationTask()
            .prepareAndRun()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAnotherModel() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()

        val inputOutputPlaces =  InputOutputPlaces.build {
            inputPlaces("p1 p2")
            outputPlaces("p4")
        }
        val placeTyping = PlaceTyping.build()

        val ocNet = ocNetFacadeBuilder.tryBuildModel(
            placeTyping = placeTyping,
            inputOutputPlaces = inputOutputPlaces
        ) {

            place("p1") {}.arcTo(transition("t1"))

            place("p2") { }
                .arcTo(transition("t1"))
                .arcTo(place("p3") { }) {
                    multiplicity = 2
                }
                .arcTo(transition { }) {
                    multiplicity = 2
                }
                .arcTo(place("p4") {}) {
                    multiplicity = 2
                }
        }.requireConsistentOCNet()
        assertNotNull(ocNet) {
            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
        }
        requireNotNull(ocNet)

        val places = ocNet.places
        val transitions = ocNet.transitions
        val simulationParamsTypeABuilder = SimulationParamsTypeABuilder(ocNet)
            .withPlaceTyping(placeTyping = placeTyping)
            .withInputOutput(inputOutputPlaces)
            .withInitialMarking(
                PlainMarking.of {
                    put(places["p1"], 10)
                    put(places["p2"], 4)
                }
            )
            .withTimeIntervals(
                IntervalFunction.create {
                    put(
                        transitions["t1"], TransitionTimes(
                            duration = 10..15,
                            pauseBeforeNextOccurence = 10..10
                        )
                    )
                    put(
                        transitions["t2"], TransitionTimes(
                            duration = 0..5,
                            pauseBeforeNextOccurence = 0..0
                        )
                    )
                }
            )
            .withRandomSeed(randomSeed = 42L)
            .useRandom(true)

        val simulatorCreator = SimulationCreator(
            simulationParams = simulationParamsTypeABuilder.build(),
            executionConditions = ConsoleDebugExecutionConditions(),
            logger = LoggerFactoryDefault
        )
        simulatorCreator
            .createSimulationTask()
            .prepareAndRun()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAnotherModelVariable() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val inputOutputPlaces =  InputOutputPlaces.build {
            inputPlaces("p1 p2")
            outputPlaces("p4")
        }
        val placeTyping = PlaceTyping.build()

        val ocNet = ocNetFacadeBuilder.tryBuildModel(
            placeTyping = placeTyping,
            inputOutputPlaces = inputOutputPlaces
        ) {
            place("p1") { }.arcTo(transition("t1"))
            place("p2") { }
                .arcTo(transition("t1"))
                .arcTo(place("p3") { }) {
                    multiplicity = 2
                }
                .variableArcTo(transition { })
                .variableArcTo(place("p4") { })
        }.requireConsistentOCNet()
        assertNotNull(ocNet) {
            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
        }
        requireNotNull(ocNet)

        val places = ocNet.places
        val transitions = ocNet.transitions
        val simulationParamsTypeABuilder = SimulationParamsTypeABuilder(ocNet)
            .withPlaceTyping(placeTyping)
            .withInitialMarking(
                PlainMarking.of {
                    put(places["p1"], 10)
                    put(places["p2"], 4)
                }
            )
            .withInputOutput(inputOutputPlaces)
            .withTimeIntervals(
                IntervalFunction.create {
                    put(
                        transitions["t1"], TransitionTimes(
                            duration = 10..15,
                            pauseBeforeNextOccurence = 10..10
                        )
                    )
                    put(
                        transitions["t2"], TransitionTimes(
                            duration = 0..5,
                            pauseBeforeNextOccurence = 0..0
                        )
                    )
                }
            )
            .withRandomSeed(randomSeed = 42L)
            .useRandom(false)

        val simulatorCreator = SimulationCreator(
            simulationParams = simulationParamsTypeABuilder.build(),
            executionConditions = ConsoleDebugExecutionConditions(),
            logger = LoggerFactoryDefault
        )
        simulatorCreator
            .createSimulationTask()
            .prepareAndRun()
    }
}
