package simulation

import kotlinx.coroutines.sync.Mutex
import model.Binding
import model.utils.EnabledBindingCollectorVisitorDFS
import model.WellFormedOCNet
import model.utils.TokenInitializerVisitorDFS

class SimulationTask(private val ocNet : WellFormedOCNet) {
    private val executionLock: Mutex = Mutex()

    suspend fun run(
        executionConditions: ExecutionConditions,
        logger: Logger,
    ) {
        if (executionLock.isLocked) return
        executionLock.lock()

        val enabledBindingCollectorVisitor = EnabledBindingCollectorVisitorDFS()
        val tokenInitializerVisitor = TokenInitializerVisitorDFS()

        logger.onStart()
        // initialize all places with their initial tokens
        for (inputPlace in ocNet.inputPlaces) {
            inputPlace.acceptVisitor(tokenInitializerVisitor)
        }

        var stepIndex: Int = 0

        while (!executionConditions.checkTerminateConditionSatisfied(ocNet)) {
            logger.onExecutionStep(stepIndex)
            // find enabled bindings throughout all the graph
            for (inputPlace in ocNet.inputPlaces) {
                inputPlace.acceptVisitor(enabledBindingCollectorVisitor)
            }
            val collectedEnabledBindings = enabledBindingCollectorVisitor.getEnabledBindings()
            enabledBindingCollectorVisitor.clear()
            enabledBindingCollectorVisitor.cleanStack()
            if (collectedEnabledBindings.isEmpty()) {
                break
            }

            val selectedBinding: Binding = executionConditions.selectBindingToExecute(collectedEnabledBindings)
            selectedBinding.execute(stepIndex++, logger.loggingEnabled)
            logger.logBindingExecution(selectedBinding)
            executionConditions.checkIfSuspend(ocNet, lastExecutionBinding = selectedBinding)
        }
        logger.onEnd()

        executionLock.unlock()
    }
}
