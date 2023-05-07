package simulation.client

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import model.StaticCoreOcNet
import simulation.ProcessedSimulationConfig
import simulation.SimpleExecutionConditions
import simulation.SimulationCreator
import simulation.utils.createParams

class ClientSimTask(
    private val staticCoreOcNet: StaticCoreOcNet,
    private val config: ProcessedSimulationConfig
) {
    private val simulationCreator = SimulationCreator(
        simulationParams = createParams(staticCoreOcNet, config),
        executionConditions = SimpleExecutionConditions(),
    )
    private val task = simulationCreator.createSimulationTask()
    private val myCoroutineScope = MyCoroutineScope()

    private var jobba : Job? = null

    fun launch(simCallback: SimCallback) {
        if (jobba?.isActive == true) return

        jobba = myCoroutineScope.launch {
            task.prepareAndRun()
            simCallback.onFinishedSimulation()
        }
    }
}
