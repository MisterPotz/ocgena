package ru.misterpotz.ocgena.original

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.simulation_old.config.*
import ru.misterpotz.ocgena.simulation_old.logging.fastNoDevSetup
import ru.misterpotz.ocgena.testing.buildSimplestOCNetNoVar
import ru.misterpotz.ocgena.testing.simComponentOld
import ru.misterpotz.ocgena.testing.simTask
import java.util.concurrent.TimeUnit

class OCNetTest {
    @Test()
    fun checkInitialMarkingIsApplied() = runTest {
        val ocNet = buildSimplestOCNetNoVar()
        val settingsConfig = deserializeResourceByClassWithPath<SettingsSimulationConfig>(DEFAULT_SETTINGS)
        val simConfig = SimulationConfig.fromNetAndSettings(ocNet, settingsConfig)
            .withInitialMarking {
                put("p1", 5)
            }
            .withGenerationPlaces {
                put("p1", 2)
            }

        val component = simComponentOld(simConfig)
        val simTask = simTask(component)

        simTask.prepareRun()
        val objectTokenRealAmountRegistry = component.objectTokenRealAmountRegistry()

        assertTrue(
            objectTokenRealAmountRegistry.getRealAmountAt("p1") == 5,
            "seems initial marking scheme doesn't work"
        )
    }

    @Test
    @Timeout(value = 1, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    fun simpleRunPerforms() = runTest {
        val ocNet = buildSimplestOCNetNoVar()
        val settingsConfig = deserializeResourceByClassWithPath<SettingsSimulationConfig>(DEFAULT_SETTINGS)

        val simConfig = SimulationConfig.fromNetAndSettings(ocNet, settingsConfig)
            .withInitialMarking {
                put("p1", 5)
            }
            .withGenerationPlaces {
                put("p1", 2)
            }

        val component = simComponentOld(simConfig)
        val simTask = simTask(component)

        simTask.prepareAndRunAll()
    }

    @Test
    @Timeout(value = 10, threadMode = Timeout.ThreadMode.SEPARATE_THREAD, unit = TimeUnit.SECONDS)
    fun simpleRunPerformHugeTokenAmount() = runTest {
        val ocNet = buildSimplestOCNetNoVar()
        val settingsConfig = deserializeResourceByClassWithPath<SettingsSimulationConfig>(DEFAULT_SETTINGS)

        val simConfig = SimulationConfig.fromNetAndSettings(ocNet, settingsConfig)
            .withInitialMarking {
                put("p1", 100000)
            }
            .withGenerationPlaces {
                put("p1", 100000)
            }

        val component = simComponentOld(
            simConfig,
            developmentDebugConfig = fastNoDevSetup()
                .copy(
                    markEachNStep = true,
                    stepNMarkGranularity = 1000
                )
        )
        val simTask = simTask(component)

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
