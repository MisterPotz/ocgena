package model

import ru.misterpotz.ocgena.error.prettyPrint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import simulation.utils.SimulationParamsBuilder
import ru.misterpotz.ocgena.simulation.config.TransitionInstancesTimesSpec
import ru.misterpotz.ocgena.simulation.config.TransitionInstanceTimes
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry
import ru.misterpotz.ocgena.simulation.SimpleExecutionConditions
import simulation.*

class OCNetTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test()
    fun testRunSimpleModel() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val placeTypeRegistry = PlaceTypeRegistry.build {
            inputPlaces("p1")
            outputPlaces("p2")
        }
        val placeToObjectTypeRegistry = PlaceToObjectTypeRegistry.build()

        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
            placeToObjectTypeRegistry = placeToObjectTypeRegistry,
            placeTypeRegistry = placeTypeRegistry
        ) {
            place { }
                .arcTo(transition { })
                .arcTo(place { })
        }.requireConsistentOCNet()

        assertNotNull(
            ocNet,
            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
        )

        val simulationParamsBuilder = SimulationParamsBuilder(ocNet)
            .withPlaceTypingAndInitialMarking(
                placeToObjectTypeRegistry = PlaceToObjectTypeRegistry.build(),
                PlainMarking.of {
                    put("p1", 10)
                }
            )
            .withOcNetType(ocNetType = OcNetType.AALST)
            .withInputOutput(
                PlaceTypeRegistry.build {
                    inputPlaces("p1")
                    outputPlaces("p2")
                }
            )
            .withTimeIntervals(
                TransitionInstancesTimesSpec.create {
                    put(
                        "t1", TransitionInstanceTimes(
                            duration = 10..15,
                            timeUntilNextInstanceIsAllowed = 0..0
                        )
                    )
                }
            )

        val simulatorCreator = SimulationCreator(
            simulationParams = simulationParamsBuilder.build(),
            executionConditions = SimpleExecutionConditions(),
            logger = LoggerFactoryDefault,
            dumpState = false
        )

        val task = simulatorCreator
            .createSimulationTask()
            .prepareAndRunAll()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAnotherModel() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()

        val placeTypeRegistry =  PlaceTypeRegistry.build {
            inputPlaces("p1 p2")
            outputPlaces("p4")
        }
        val placeToObjectTypeRegistry = PlaceToObjectTypeRegistry.build()

        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
            placeToObjectTypeRegistry = placeToObjectTypeRegistry,
            placeTypeRegistry = placeTypeRegistry
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

        val simulationParamsBuilder = SimulationParamsBuilder(ocNet)
            .withInputOutput(placeTypeRegistry)
            .withPlaceTypingAndInitialMarking(
                placeTyping = placeToObjectTypeRegistry,
                PlainMarking.of {
                    put("p1", 10)
                    put("p2", 4)
                }
            )
            .withOcNetType(OcNetType.AALST)
            .withTimeIntervals(
                TransitionInstancesTimesSpec.create {
                    put(
                        "t1", TransitionInstanceTimes(
                            duration = 10..15,
                            timeUntilNextInstanceIsAllowed = 10..10
                        )
                    )
                    put(
                        "t2", TransitionInstanceTimes(
                            duration = 0..5,
                            timeUntilNextInstanceIsAllowed = 0..0
                        )
                    )
                }
            )
            .withRandomSeed(randomSeed = 42)
            .useRandom(true)

        val simulatorCreator = SimulationCreator(
            simulationParams = simulationParamsBuilder.build(),
            executionConditions = SimpleExecutionConditions(),
            logger = LoggerFactoryDefault
        )
        simulatorCreator
            .createSimulationTask()
            .prepareAndRunAll()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAnotherModelVariable() = runTest {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val placeTypeRegistry =  PlaceTypeRegistry.build {
            inputPlaces("p1 p2")
            outputPlaces("p4")
        }
        val placeToObjectTypeRegistry = PlaceToObjectTypeRegistry.build()

        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
            placeToObjectTypeRegistry = placeToObjectTypeRegistry,
            placeTypeRegistry = placeTypeRegistry
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

        val simulationParamsBuilder = SimulationParamsBuilder(ocNet)
            .withPlaceTypingAndInitialMarking(
                placeToObjectTypeRegistry = placeToObjectTypeRegistry,
                PlainMarking.of {
                    put("p1", 10)
                    put("p2", 4)
                }
            )
            .withOcNetType(OcNetType.AALST)
            .withInputOutput(placeTypeRegistry)
            .withTimeIntervals(
                TransitionInstancesTimesSpec.create {
                    put(
                        "t1", TransitionInstanceTimes(
                            duration = 10..15,
                            timeUntilNextInstanceIsAllowed = 10..10
                        )
                    )
                    put(
                        "t2", TransitionInstanceTimes(
                            duration = 0..5,
                            timeUntilNextInstanceIsAllowed = 0..0
                        )
                    )
                }
            )
            .withRandomSeed(randomSeed = 42)
            .useRandom(false)

        val simulatorCreator = SimulationCreator(
            simulationParams = simulationParamsBuilder.build(),
            executionConditions = SimpleExecutionConditions(),
            logger = LoggerFactoryDefault
        )
        simulatorCreator
            .createSimulationTask()
            .prepareAndRunAll()
    }
}
