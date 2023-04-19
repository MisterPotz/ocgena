package simulation

import kotlinx.coroutines.sync.Mutex
import model.ActiveBinding
import model.BindingExecutor
import model.WellFormedOCNet
import model.utils.EnabledBindingCollectorVisitorDFS

class SimulationParams(
    val initialMarking: Marking,
)

class SimulationTask(
    private val ocNet: WellFormedOCNet,
    private val executionConditions: ExecutionConditions,
    private val logger: Logger,
    private val simulationParams: SimulationParams,
    private val bindingExecutor: BindingExecutor
) {
    private val executionLock: Mutex = Mutex()

    private fun prepare() {
        ocNet.strictSetMarking(simulationParams.initialMarking)
    }

    private suspend fun run() {
        val enabledBindingCollectorVisitor = EnabledBindingCollectorVisitorDFS()

        var stepIndex: Int = 0

        while (!executionConditions.checkTerminateConditionSatisfied(ocNet)) {
            logger.onExecutionStep(stepIndex)
            // find enabled bindings throughout all the graph
            for (inputPlace in ocNet.inputPlaces) {
                inputPlace.acceptVisitor(enabledBindingCollectorVisitor)
            }
            val collectedEnabledBindings = enabledBindingCollectorVisitor.getEnabledBindings()
            enabledBindingCollectorVisitor.fullReset()
            if (collectedEnabledBindings.isEmpty()) {
                break
            }

            val selectedBinding: ActiveBinding = executionConditions.selectBindingToExecute(collectedEnabledBindings)
            selectedBinding.execute(stepIndex++, logger.loggingEnabled)
            logger.logBindingExecution(selectedBinding)
            executionConditions.checkIfSuspend(ocNet, lastExecutionBinding = selectedBinding)
        }
    }

    suspend fun prepareAndRun() {
        if (executionLock.isLocked) return
        executionLock.lock()
        logger.onStart()
        prepare()
        run()
        logger.onEnd()
        executionLock.unlock()
    }
}
