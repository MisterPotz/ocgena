package ru.misterpotz.ocgena

import org.junit.jupiter.api.Assertions
import ru.misterpotz.ocgena.simulation_old.di.SimulationComponent
import ru.misterpotz.ocgena.testing.ExpectedStepState
import ru.misterpotz.ocgena.testing.SimConfigToSimComponentFancyBlock
import ru.misterpotz.ocgena.testing.TestingDBLogger

class SimulationSequenceHelper {
        private var expectedStepStates: List<ExpectedStepState> = listOf()

        private val simulatedStepsSlice
            get() = expectedStepStates.slice(1..expectedStepStates.lastIndex)

        private val transitionSequence
            get() = simulatedStepsSlice.map { it.chosenTransition!! }
        private val timeSelectionSequence
            get() = simulatedStepsSlice.map { it.timeClockIncrement.toInt() }

        private val testingDBLogger = TestingDBLogger()
        fun SimConfigToSimComponentFancyBlock.setupReceiver() {
            setTransitionSelectionSequence(transitionSequence)
            setTimeSelectionRandom(timeSelectionSequence)
            setDbLogger(testingDBLogger)
            dumpStepEndMarking = true
            dumpTimePnMarking = true
        }

        fun setExpectedStepStates(list: List<ExpectedStepState>): SimulationSequenceHelper {
            this.expectedStepStates = list
            return this
        }

        fun setup(simConfigToSimComponentFancyBlock: SimConfigToSimComponentFancyBlock) {
            with(simConfigToSimComponentFancyBlock) {
                setupReceiver()
            }
        }
        
        fun assertTestingDBLogsToExpected(simulationComponent: SimulationComponent) {
            Assertions.assertEquals(
                simulatedStepsSlice.map { true },
                simulatedStepsSlice
                    .zip(testingDBLogger.stepLogs) { expected, realLog ->
                        expected.compareToStepLog(realLog, simulationComponent)
                    }
            )
        }
    }