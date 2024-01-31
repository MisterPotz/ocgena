package ru.misterpotz.ocgena.timepn

import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.serialization.ModelYamlCreator
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.config.timepn.toTimePNData
import ru.misterpotz.ocgena.simulation.config.timepn.toTimePNTimes
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunchImpl
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarkingImpl
import ru.misterpotz.ocgena.utils.buildMutableMap

class TimePNStepExecutorTest {

    @ArgumentsSource(SimConfigTestProvider::class)
    @ParameterizedTest
    fun `when preparing for the run each transition marking is assigned with eft and lft with default provided`(
        simulationConfig: SimulationConfig
    ) {
        val starterTimePNSpec = TransitionsTimePNSpec(
            default = (10..10).toTimePNTimes(),
            transitionToTimeSpec = buildMutableMap {
                put("t1", (1..5).toTimePNTimes())
                put("t2", (4..10).toTimePNTimes())
            }
        )

        val expectedMarking = TimePNTransitionMarkingImpl(buildMutableMap {
            put("t1", (1..5L).toTimePNData())
            put("t2", (4..10L).toTimePNData())
            put("t3", (10..10L).toTimePNData())
        })

        val simComponent = simulationConfig
            .asTimePNwithSpec(starterTimePNSpec).toSimComponent()
        val taskPreparator = simComponent.simulationTaskPreparator()
        taskPreparator.prepare()

        val currentMarking = simComponent.timePNTransitionMarking()

        Assertions.assertEquals(expectedMarking, currentMarking)
    }

    @ArgumentsSource(SimConfigTestProvider::class)
    @ParameterizedTest
    fun `when preparing for the run each transition marking is assigned with eft and lft without default provided`(
        simulationConfig: SimulationConfig
    ) {
        val starterTimePNSpec = TransitionsTimePNSpec(
            default = null,
            transitionToTimeSpec = buildMutableMap {
                put("t1", (1..5).toTimePNTimes())
                put("t2", (4..10).toTimePNTimes())
            }
        )

        val expectedMarking = TimePNTransitionMarkingImpl(buildMutableMap {
            put("t1", (1..5L).toTimePNData())
            put("t2", (4..10L).toTimePNData())
            put("t3", (5..10L).toTimePNData())
        })

        val simComponent = simulationConfig
            .asTimePNwithSpec(starterTimePNSpec).toSimComponent()
        val taskPreparator = simComponent.simulationTaskPreparator()
        taskPreparator.prepare()

        val currentMarking = simComponent.timePNTransitionMarking()

        Assertions.assertEquals(expectedMarking, currentMarking)
    }


    @ArgumentsSource(SimConfigTestProvider::class)
    @ParameterizedTest
    fun `clocks changes affect only transitions that are enabled by marking`(simulationConfig: SimulationConfig) {
        val starterTimePNSpec = TransitionsTimePNSpec(
            default = (10..20).toTimePNTimes(),
        )

        val expectedMarking = TimePNTransitionMarkingImpl(buildMutableMap {
            put("t1", (10..20L).toTimePNData(clock = 15))
            put("t2", (10..20L).toTimePNData(clock = 15))
            put("t3", (10..20L).toTimePNData(clock = 0))
        })

        val simComponent = simulationConfig
            .asTimePNwithSpec(starterTimePNSpec)
            .toSimComponent(
                randomInstance = createPartiallyPredefinedRandSeq(
                    seq = listOf(15)
                )
            )
            .addTokens {
                forPlace("p1", amount = 2)
                forPlace("p2", 1)
                forPlace("o1", 0)
            }

        val newTimeDeltaInteractor = simComponent.newTimeDeltaInteractor()
        simComponent.simulationTask().prepareRun()
        newTimeDeltaInteractor.generateAndShiftTimeDelta()

        Assertions.assertEquals(expectedMarking, simComponent.timePNTransitionMarking())
    }

    @ParameterizedTest
    @ArgumentsSource(SimConfigTestProvider::class)
    fun `clocks of transitions with common preplaces are reset while others remain unaffected`(simulationConfig: SimulationConfig) =
        runTest {
            val starterTimePNSpec = TransitionsTimePNSpec(
                default = (10..20).toTimePNTimes(),
            )

            val expectedMarking = TimePNTransitionMarkingImpl(buildMutableMap {
                put("t1", (10..20L).toTimePNData(clock = 15))
                put("t2", (10..20L).toTimePNData(clock = 0))
                put("t3", (10..20L).toTimePNData(clock = 0))
            })

            val simComponent = simulationConfig
                .asTimePNwithSpec(starterTimePNSpec)
                .toSimComponentFancy {
                    setTimeSelectionRandom(listOf(15))
                    setTransitionSelectionRandom(listOf(2))
                }
                .addTokens {
                    forPlace("p1", 2)
                    forPlace("p2", 2)
                    forPlace("o1", 2)
                }
            simComponent.simulationTask().prepareRun()
            simComponent.stepExecutor().executeStep(mockk())
            Assertions.assertEquals(expectedMarking, simComponent.timePNTransitionMarking())
        }


    @ParameterizedTest
    @ArgumentsSource(SimConfigTestProvider::class)
    fun `when transition becomes enabled by marking clocks tick`(simulationConfig: SimulationConfig) = runTest {
        val starterTimePNSpec = TransitionsTimePNSpec(
            default = (10..20).toTimePNTimes(),
        )

        val expectedMarking = TimePNTransitionMarkingImpl(buildMutableMap {
            put("t1", (10..20L).toTimePNData(clock = 0))
            put("t2", (10..20L).toTimePNData(clock = 0))
            put("t3", (10..20L).toTimePNData(clock = 0))
        })

        val expectedBunch = SparseTokenBunchImpl.makeBuilder {
            forPlace("p1") {
                realTokens = 1
            }
        }.buildTokenBunch()

        val simComponent = simulationConfig
            .asTimePNwithSpec(starterTimePNSpec)
            .toSimComponentFancy {
                setTimeSelectionRandom(listOf(10))
                setTransitionSelectionRandom(listOf(0))
            }

        simComponent.simulationTask().prepareRun()
        simComponent.addTokens {
            forPlace("p1", 2)
        }
        simComponent.stepExecutor().executeStep(mockk())


        Assertions.assertTrue(
            simComponent.tokenBunch()
                .narrowTo(listOf("p1"))
                .projectBunchAmountsEqual(expectedBunch)
        )
        Assertions.assertEquals(expectedMarking, simComponent.timePNTransitionMarking())
    }

    @Test
    fun `during the beginning of step the clocks of transitions disabled on last step are reset and eft,lft reassigned`() {
        throw NotImplementedError()
    }

    companion object {
        data class TestData(val simulationConfig: SimulationConfig)

        private val defaultConfig: ConfigBuilderBlockScope = {
            this.ocNetStruct = buildOCNet {
                ModelYamlCreator.buildingBlockTwoInTwoOutMiddle().installOnto(this)
            }
            this.semanticsType = SimulationSemanticsType.SIMPLE_TIME_PN
            this.ocNetType = OcNetType.LOMAZOVA
        }

        class SimConfigTestProvider : ArgumentsProvider by createArgProvider(
            list = listOf(
                buildConfig(defaultConfig),
                buildConfig {
                    defaultConfig()
                    this.ocNetType = OcNetType.AALST
                }
            )
        )
    }
}