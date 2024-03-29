package ru.misterpotz.ocgena.dsl.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import ru.misterpotz.ocgena.dsl.tool.buildSimplestOCNetNoVar
import ru.misterpotz.ocgena.dsl.tool.component
import ru.misterpotz.ocgena.dsl.tool.defaultSimConfig
import ru.misterpotz.ocgena.dsl.tool.simTask
import ru.misterpotz.ocgena.simulation.config.*
import ru.misterpotz.ocgena.simulation.logging.fastConsistencyDevSetup
import ru.misterpotz.ocgena.simulation.logging.fastFullDev
import ru.misterpotz.ocgena.simulation.logging.fastNoDevSetup

class OCNetTest {
    @Test()
    fun checkInitialMarkingIsApplied() {
        val ocNet = buildSimplestOCNetNoVar()

        val config = defaultSimConfig(ocNet).copy(
            initialMarking = MarkingScheme.of {
                put("p1", 10)
            }
        )

        val component = component(config)
        val simTask = simTask(component)
        simTask.prepareRun()
        val objectTokenRealAmountRegistry = component.objectTokenRealAmountRegistry()

        assertTrue(
            objectTokenRealAmountRegistry.getRealAmountAt("p1") == 10,
            "seems initial marking scheme doesn't work"
        )
    }

    @Test
    @Timeout(value = 1,  threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    fun simpleRunPerforms() {
        val ocNet = buildSimplestOCNetNoVar()

        val config = defaultSimConfig(ocNet)
            .copy(
                initialMarking = MarkingScheme.of {
                    put("p1", 1)
                },
                transitionInstancesTimesSpec = TransitionInstancesTimesSpec(
                    defaultTransitionTimeSpec = TransitionInstanceTimes(
                        duration = Duration(10..10),
                        timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(10..10)
                    )
                ),
                tokenGeneration = TokenGenerationConfig(
                    placeIdToGenerationTarget = MarkingScheme.of {
                        put("p1", 1)
                    }
                )
            )
        val component = component(config)
        val simTask = simTask(component)

        simTask.prepareAndRunAll()
    }

    @Test
    fun simpleRunPerformHugeTokenAmount() {
        val ocNet = buildSimplestOCNetNoVar()

        val config = defaultSimConfig(ocNet).copy(
            initialMarking = MarkingScheme.of {
                put("p1", 100000)
            },
            tokenGeneration = TokenGenerationConfig(
                placeIdToGenerationTarget = MarkingScheme.of {
                    put("p1", 100000)
                },
            ),

            )
        val component = component(
            config,
            developmentDebugConfig = fastNoDevSetup().copy(markEachNStep = true, stepNMarkGranularity = 1000)
        )
        val simTask = simTask(component)

        (0..20).shuffled()
        simTask.prepareAndRunAll()
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
