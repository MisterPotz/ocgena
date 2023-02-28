package model

import kotlinx.coroutines.sync.Mutex

/**
 * the net, formed with passed arguments, must already be consistent
 */
class OCNet(
    val inputPlaces : List<Place>,
    val outputPlaces : List<Place>,
    val objectTypes : List<ObjectType>
) {
    private val executionLock : Mutex = Mutex()

    interface Logger {
        // TODO: pass in to visitors to log the performance
        fun logBindingExecution(binding: Binding)
    }

    interface ExecutionConditions {

        // for terminate
        fun checkTerminateConditionSatisfied(ocNet: OCNet) : Boolean

        // TODO: for debug ability
        suspend fun checkIfSuspend(ocNet: OCNet, lastExecutionBinding: Binding)

        // TODO: this method can fulfill both interactive mode and automatic
        suspend fun selectBindingToExecute(enabledBindings : List<Binding>) : Binding
    }

    class ConsoleDebugExecutionConditions() : ExecutionConditions {
        override fun checkTerminateConditionSatisfied(ocNet: OCNet): Boolean {
            // TODO: check for terminate token amounts at place nodes
            return false
        }

        override suspend fun checkIfSuspend(ocNet: OCNet, lastExecutionBinding: Binding) {
            Unit
        }

        override suspend fun selectBindingToExecute(enabledBindings: List<Binding>): Binding {
            // by default, select the first one
            return enabledBindings.first()
        }

    }

    suspend fun run(
        executionConditions: ExecutionConditions,
        logger: Logger
    ) {
        if (executionLock.isLocked) return
        executionLock.lock()

        val enabledBindingCollectorVisitor = EnabledBindingCollectorVisitor()

        while (!executionConditions.checkTerminateConditionSatisfied(this)) {
            // find enabled bindings
            for (inputPlace in inputPlaces)  {
                inputPlace.acceptVisitor(enabledBindingCollectorVisitor)
            }
            val collectedEnabledBindings = enabledBindingCollectorVisitor.getEnabledBindings()
            enabledBindingCollectorVisitor.clear()

            if (collectedEnabledBindings.isEmpty()) {
                break
            }

            val selectedBinding = executionConditions.selectBindingToExecute(collectedEnabledBindings)
            selectedBinding.execute()

            executionConditions.checkIfSuspend(this, lastExecutionBinding = selectedBinding)
        }

        executionLock.unlock()

        // TODO: output some results somewhere after execution is finished?
    }
}
