package ru.misterpotz.ocgena.simulation_old

import ru.misterpotz.ocgena.simulation_old.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation_old.di.SimulationScope
import ru.misterpotz.ocgena.simulation_old.stepexecutor.StepExecutor
import ru.misterpotz.ocgena.simulation_old.state.SimulationStepState
import ru.misterpotz.ocgena.simulation_old.state.SimulationTime
import ru.misterpotz.ocgena.simulation_old.stepexecutor.SimulationStepLogBuilder
import ru.misterpotz.ocgena.simulation_old.stepexecutor.SimulationStepLogBuilderCreator
import ru.misterpotz.ocgena.simulation_old.structure.SimulatableOcNetInstance
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
    fun simulationLogBuilder(): SimulationStepLogBuilder
    fun onNewStep()
    fun markFinished()
}

@SimulationScope
class SimulationStateProviderImpl @Inject constructor(
    private val simulatableOcNetInstance: SimulatableOcNetInstance,
    private val simulationStepLogBuilderCreator: SimulationStepLogBuilderCreator
) : SimulationStateProvider {
    private val simulationTime = SimulationTime()
    private val simulationStepState = SimulationStepState()
    override var status: Status = Status.EXECUTING
    private var simulationStepLogBuilder: SimulationStepLogBuilder? = null

    override fun getSimulationTime(): SimulationTime {
        return simulationTime
    }

    override fun getSimulationStepState(): SimulationStepState {
        return simulationStepState
    }

    override fun simulatableOcNetInstance(): SimulatableOcNetInstance {
        return simulatableOcNetInstance
    }

    override fun simulationLogBuilder(): SimulationStepLogBuilder {
        return simulationStepLogBuilder!!
    }

    override fun onNewStep() {
        simulationStepState.onNewStep()
        simulationStepLogBuilder = simulationStepLogBuilderCreator.create()
    }

    override fun markFinished() {
        status = Status.FINISHED
    }
}

class SimulationTaskStepExecutor @Inject constructor(
    private val stepExecutor: StepExecutor
) {

    suspend fun executeStep(executionContinuation: ExecutionContinuation) {
        stepExecutor.executeStep(executionContinuation)
    }
}
