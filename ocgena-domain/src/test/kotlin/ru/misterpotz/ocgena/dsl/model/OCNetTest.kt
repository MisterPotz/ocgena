package ru.misterpotz.ocgena.dsl.model

import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder
import ru.misterpotz.ocgena.validation.OCNetChecker
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.ocgena.error.prettyPrint
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.config.*
import ru.misterpotz.ocgena.simulation.di.SimulationComponent

class OCNetTest {
    @Test()
    fun testRunSimpleModel() {
        val ocNet = OCNetBuilder().defineAtoms {
            "p1".p { input }
                .arc("t1".t)
                .arc("p2".p { output })
        }
        val errors = OCNetChecker(ocNet).checkConsistency()


        assertTrue(
            errors.isEmpty(),
            "ocNet is null, detected errors: ${errors.prettyPrint()}"
        )

        val simulationConfig = SimulationConfig(
            ocNet,
            initialMarking = MarkingScheme.of {
                put("p1", 10)
            },
            transitionInstancesTimesSpec = TransitionInstancesTimesSpec(
                defaultTransitionTimeSpec = TransitionInstanceTimes(
                    duration = Duration(2..10),
                    timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(10..100)
                )
            ),
            randomSeed = 45,
            nodeToLabelRegistry = NodeToLabelRegistry(),
            tokenGeneration = TokenGenerationConfig(
                defaultPeriod = Period(100..120),
                placeIdToGenerationTarget = MarkingScheme.of {
                    put("p1", 15)
                }
            ),
            ocNetType = OcNetType.AALST
        )

        val simulationComponent = SimulationComponent.defaultCreate(
            simulationConfig = simulationConfig,
            componentDependencies = DomainComponent.create()
        )
        val task = simulationComponent.simulationTask()

        task.prepareAndRunAll()
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun testAnotherModel() = runTest {
//        val ocNetFacadeBuilder = OCNetFacadeBuilder()
//
//        val placeTypeRegistry = PlaceTypeRegistry.build {
//            inputPlaces("p1 p2")
//            outputPlaces("p4")
//        }
//        val placeToObjectTypeRegistry = PlaceToObjectTypeRegistry.build()
//
//        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
//            placeToObjectTypeRegistry = placeToObjectTypeRegistry,
//            placeTypeRegistry = placeTypeRegistry
//        ) {
//
//            place("p1") {}.arcTo(transition("t1"))
//
//            place("p2") { }
//                .arcTo(transition("t1"))
//                .arcTo(place("p3") { }) {
//                    multiplicity = 2
//                }
//                .arcTo(transition { }) {
//                    multiplicity = 2
//                }
//                .arcTo(place("p4") {}) {
//                    multiplicity = 2
//                }
//        }.requireConsistentOCNet()
//        assertNotNull(ocNet) {
//            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
//        }
//        requireNotNull(ocNet)
//
//        val simulationParamsBuilder = SimulationParamsBuilder(ocNet)
//            .withInputOutput(placeTypeRegistry)
//            .withPlaceTypingAndInitialMarking(
//                placeTyping = placeToObjectTypeRegistry,
//                PlainMarking.of {
//                    put("p1", 10)
//                    put("p2", 4)
//                }
//            )
//            .withOcNetType(OcNetType.AALST)
//            .withTimeIntervals(
//                TransitionInstancesTimesSpec.create {
//                    put(
//                        "t1", TransitionInstanceTimes(
//                            duration = 10..15,
//                            timeUntilNextInstanceIsAllowed = 10..10
//                        )
//                    )
//                    put(
//                        "t2", TransitionInstanceTimes(
//                            duration = 0..5,
//                            timeUntilNextInstanceIsAllowed = 0..0
//                        )
//                    )
//                }
//            )
//            .withRandomSeed(randomSeed = 42)
//            .useRandom(true)
//
//        val simulatorCreator = SimulationCreator(
//            simulationParams = simulationParamsBuilder.build(),
//            executionConditions = SimpleExecutionConditions(),
//            logger = LoggerFactoryDefault
//        )
//        simulatorCreator
//            .createSimulationTask()
//            .prepareAndRunAll()
//    }
//
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun testAnotherModelVariable() = runTest {
//        val ocNetFacadeBuilder = OCNetFacadeBuilder()
//        val placeTypeRegistry = PlaceTypeRegistry.build {
//            inputPlaces("p1 p2")
//            outputPlaces("p4")
//        }
//        val placeToObjectTypeRegistry = PlaceToObjectTypeRegistry.build()
//
//        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
//            placeToObjectTypeRegistry = placeToObjectTypeRegistry,
//            placeTypeRegistry = placeTypeRegistry
//        ) {
//            place("p1") { }.arcTo(transition("t1"))
//            place("p2") { }
//                .arcTo(transition("t1"))
//                .arcTo(place("p3") { }) {
//                    multiplicity = 2
//                }
//                .variableArcTo(transition { })
//                .variableArcTo(place("p4") { })
//        }.requireConsistentOCNet()
//        assertNotNull(ocNet) {
//            "ocNet is null, detected errors: ${ocNetFacadeBuilder.definedNetData!!.errors.prettyPrint()}"
//        }
//        requireNotNull(ocNet)
//
//        val simulationParamsBuilder = SimulationParamsBuilder(ocNet)
//            .withPlaceTypingAndInitialMarking(
//                placeToObjectTypeRegistry = placeToObjectTypeRegistry,
//                PlainMarking.of {
//                    put("p1", 10)
//                    put("p2", 4)
//                }
//            )
//            .withOcNetType(OcNetType.AALST)
//            .withInputOutput(placeTypeRegistry)
//            .withTimeIntervals(
//                TransitionInstancesTimesSpec.create {
//                    put(
//                        "t1", TransitionInstanceTimes(
//                            duration = 10..15,
//                            timeUntilNextInstanceIsAllowed = 10..10
//                        )
//                    )
//                    put(
//                        "t2", TransitionInstanceTimes(
//                            duration = 0..5,
//                            timeUntilNextInstanceIsAllowed = 0..0
//                        )
//                    )
//                }
//            )
//            .withRandomSeed(randomSeed = 42)
//            .useRandom(false)
//
//        val simulatorCreator = SimulationCreator(
//            simulationParams = simulationParamsBuilder.build(),
//            executionConditions = SimpleExecutionConditions(),
//            logger = LoggerFactoryDefault
//        )
//        simulatorCreator
//            .createSimulationTask()
//            .prepareAndRunAll()
//    }
}
