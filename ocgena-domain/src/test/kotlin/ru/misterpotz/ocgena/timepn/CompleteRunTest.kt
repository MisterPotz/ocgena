package ru.misterpotz.ocgena.timepn

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.ocgena.SimulationSequenceHelper
import ru.misterpotz.ocgena.createArgProvider
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.config.timepn.toTimePNTimes
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.testing.*

class CompleteRunTest {

    @ParameterizedTest
    @ArgumentsSource(SimulationSequenceCaseProvider::class)
    fun `complete run test`(expectedStates: List<ExpectedStepState>) = runTest {
        val simulationSequenceHelper = SimulationSequenceHelper().setExpectedStepStates(expectedStates)

        val config = buildConfig {
            this.ocNetStruct = buildOCNet {
                buildingBlockTwoInTwoOutMiddle().installOnto(this)
                "t3".arc("p3") { norm; multiplicity = 1 }
            }
            this.ocNetType = OcNetType.LOMAZOVA
            this.semanticsType = SimulationSemanticsType.SIMPLE_TIME_PN
        }.withInitialMarking {
            put("p1", 3)
        }.asTimePNwithSpec(TransitionsTimePNSpec(default = (0..10).toTimePNTimes()))

        val simComp = config.toSimComponentFancy {
            simulationSequenceHelper.setup(this)
        }
        simComp.simulationTask().prepareAndRunAll()

        simulationSequenceHelper.assertTestingDBLogsToExpected(simComp)
    }


    companion object {
        class SimulationSequenceCaseProvider : ArgumentsProvider by createArgProvider(
            listOf(
                listOf(
                    // initial state
                    ExpectedStepState(
                        0,
                        null,
                        {
                            "p1" to 3
                        },
                        {
                            "t1" to 0
                        }
                    ),
                    ExpectedStepState(
                        5L,
                        "t1",
                        {
                            "p1" to 2
                            "p2" to 1
                            "o1" to 1
                        },
                        {}
                    ),
                    ExpectedStepState(
                        2L,
                        "t1",
                        {
                            "p1" to 1
                            "p2" to 2
                            "o1" to 2
                        },
                        {
                            "t1" to 0
                            "t2" to 2
                            "t3" to 2
                        }
                    ),
                    ExpectedStepState(
                        8L,
                        chosenTransition = "t2",
                        {
                            "p1" to 1
                            "p2" to 1
                            "p3" to 1
                            "o1" to 2
                        },
                        {
                            "t1" to 8
                            "t2" to 0
                            "t3" to 0
                        }
                    ),
                    ExpectedStepState(
                        2L,
                        "t3",
                        {
                            "p1" to 1
                            "p2" to 0
                            "p3" to 2
                            "o1" to 0
                            "o2" to 2
                        },
                        {
                            "t1" to 10
                            "t2" to 0L
                            "t3" to 0L
                        }
                    ),
                    ExpectedStepState(
                        0L,
                        "t1",
                        {
                            "p2" to 1
                            "p3" to 2
                            "o1" to 1
                            "o2" to 2
                        }, {
                            "t1" to 0
                            "t2" to 0
                            "t3" to 0
                        }
                    ),
                    ExpectedStepState(
                        3L,
                        "t3",
                        {
                            "p2" to 0
                            "p3" to 3
                            "o1" to 0
                            "o2" to 3
                        },
                        {
                            "t1" to 0
                            "t2" to 0
                            "t3" to 0
                        }
                    ),
                )
            )
        )
    }
}