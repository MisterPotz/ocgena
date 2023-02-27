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
        fun checkIfTerminate(ocNet: OCNet) : Boolean

        // TODO: for debug ability
        suspend fun checkIfSuspend(ocNet: OCNet, lastExecutionBinding: Binding)

        // TODO: this method can fulfill both interactive mode and automatic
        suspend fun selectBindingToExecute(enabledBindings : List<Binding>) : Binding
    }

    suspend fun run(
        executionConditions: ExecutionConditions,
        logger: Logger
    ) {
        if (executionLock.isLocked) return
        executionLock.lock()

        val enabledBindingCollectorVisitor = EnabledBindingCollectorVisitor()

        while (!executionConditions.checkIfTerminate(this)) {
            // find enabled bindings
            for (inputPlace in inputPlaces)  {
                inputPlace.acceptVisitor(enabledBindingCollectorVisitor)
            }
            val collectedEnabledBindings = enabledBindingCollectorVisitor.obtainedEnabledBindings
            val selectedBinding = executionConditions.selectBindingToExecute(collectedEnabledBindings)
            enabledBindingCollectorVisitor.clear()

            executionConditions.checkIfSuspend(this, selectedBinding)
        }

        executionLock.unlock()
    }
}
