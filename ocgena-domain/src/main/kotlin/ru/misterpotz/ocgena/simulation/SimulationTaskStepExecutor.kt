package ru.misterpotz.ocgena.simulation

import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation.di.SimulationScope
import ru.misterpotz.ocgena.simulation.stepexecutor.StepExecutor
import ru.misterpotz.ocgena.simulation.state.SimulationStepState
import ru.misterpotz.ocgena.simulation.state.SimulationTime
import ru.misterpotz.ocgena.simulation.structure.SimulatableOcNetInstance
import javax.inject.Inject

enum class Status {
    EXECUTING,
    FINISHED
}

interface SimulationStateProvider {
    val status: Status
    fun getSimulationTime(): SimulationTime
    fun getSimulationStepState(): SimulationStepState
    fun simulatableOcNetInstance(): SimulatableOcNetInstance
    fun markFinished()
}

@SimulationScope
class SimulationStateProviderImpl @Inject constructor(
    private val simulatableOcNetInstance: SimulatableOcNetInstance,
) : SimulationStateProvider {
    private val simulationTime = SimulationTime()
    private val simulationStepState = SimulationStepState()
    override var status: Status = Status.EXECUTING

    override fun getSimulationTime(): SimulationTime {
        return simulationTime
    }

    override fun getSimulationStepState(): SimulationStepState {
        return simulationStepState
    }

    override fun simulatableOcNetInstance(): SimulatableOcNetInstance {
        return simulatableOcNetInstance
    }

    override fun markFinished() {
        status = Status.FINISHED
    }
}

class SimulationTaskStepExecutor @Inject constructor(
    private val simulationStateProvider: SimulationStateProvider,
    private val stepExecutor : StepExecutor
) {

    suspend fun executeStep(executionContinuation: ExecutionContinuation) {
        stepExecutor.executeStep(executionContinuation)
    }
}
