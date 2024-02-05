package ru.misterpotz.ocgena.timepn

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.SimulationStepLog
import ru.misterpotz.ocgena.createArgProvider
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.config.timepn.toTimePNTimes
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunchImpl
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarking
import ru.misterpotz.ocgena.testing.*
import kotlin.math.exp

class CompleteRunTest {
    @ParameterizedTest
    @ArgumentsSource(SimulationSequenceCaseProvider::class)
    fun `complete run test`(expectedStates: List<StepState>) = runTest {
        val transitionSequence = expectedStates.slice(1..expectedStates.lastIndex).map { it.chosenTransition!! }
        val timeSelectionSequence =
            expectedStates.slice(1..expectedStates.lastIndex).map { it.timeClockIncrement.toInt() }

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

        val testingDBLogger = TestingDBLogger()

        val simComp = config.toSimComponentFancy {
            setTransitionSelectionSequence(transitionSequence)
            setTimeSelectionRandom(timeSelectionSequence)
            setDbLogger(testingDBLogger)
        }
        simComp.simulationTask().prepareAndRunAll()


        Assertions.assertEquals(
            expectedStates.slice(1..expectedStates.lastIndex).map { true },
            expectedStates.slice(1..expectedStates.lastIndex)
                .zip(testingDBLogger.stepLogs) { expected, realLog ->
                    expected.compareToStepLog(realLog, simComp)
                }
        )
    }

    data class StepState(
        val timeClockIncrement: Long,
        val chosenTransition: String?,
        val markingApplierBlock: SparseTokenBunchImpl.Builder.() -> Unit,
        val timeMarkingApplierBlock: TimePNTransitionMarking.SettingBlock.() -> Unit
    ) {
        fun compareToStepLog(simulationStepLog: SimulationStepLog, simulationComponent: SimulationComponent): Boolean {
            val tokenAmountStorage =
                simulationComponent.emptyTokenBunchBuilder().apply(markingApplierBlock).buildTokenBunch()
                    .tokenAmountStorage.dump()
            return timeClockIncrement == simulationStepLog.clockIncrement &&
                    chosenTransition == simulationStepLog.selectedFiredTransition &&
                    tokenAmountStorage == simulationStepLog.endStepMarkingAmounts

        }
    }

    companion object {
        class SimulationSequenceCaseProvider : ArgumentsProvider by createArgProvider(
            listOf(
                listOf(
                    // initial state
                    StepState(
                        0,
                        null,
                        {
                            "p1" to 3
                        },
                        {
                            "t1" to 0
                        }
                    ),
                    StepState(
                        5L,
                        "t1",
                        {
                            "p1" to 2
                            "p2" to 1
                            "o1" to 1
                        },
                        {}
                    ),
                    StepState(
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
                    StepState(
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
                    StepState(
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
                    StepState(
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
                    StepState(
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
//                    StepState(
//                        10L,
//                        "t2",
//                        {
//                            "p2" to 0
//                            "p3" to 4
//                            "o1" to 0
//                            "o2" to 4
//                        },
//                        {}
//                    )
                )
            )
        )
    }
}