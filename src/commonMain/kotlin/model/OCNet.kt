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

//    fun allPlaces() : List<Place> {
//
//    }
//    fun collectMarking() : Map<String, Int> {
//
//    }

    interface Logger {
        val loggingEnabled : Boolean

        fun onStart()
        // TODO: pass in to visitors to log the performance
        fun logBindingExecution(binding: Binding)

        fun onEnd()
    }

    class DebugLogger: Logger {
        private val executedBindings : MutableList<Binding> = mutableListOf()
        override val loggingEnabled: Boolean
            get() = true

        override fun onStart() {
            println("execution started")
        }

        override fun logBindingExecution(binding: Binding) {
            println("\texecute: ${binding.toString().prependIndent()}")
            executedBindings.add(binding)
        }

        override fun onEnd() {
            println("execution ended, executed bindings: ${executedBindings.size}")
        }
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
            println("\tenabled bindings: \n${enabledBindings.prettyPrint().prependIndent()}")
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

        val enabledBindingCollectorVisitor = EnabledBindingCollectorVisitorDFS()
        val tokenInitializerVisitor = TokenInitializerVisitorDFS()

        logger.onStart()
        // initialize all places with their initial tokens
        for (inputPlace in inputPlaces) {
            inputPlace.acceptVisitor(tokenInitializerVisitor)
        }

        var stepIndex : Int = 0

        while (!executionConditions.checkTerminateConditionSatisfied(this)) {
            println("Execution step: $stepIndex")
            // find enabled bindings throughout all the graph
            for (inputPlace in inputPlaces)  {
                inputPlace.acceptVisitor(enabledBindingCollectorVisitor)
            }
            val collectedEnabledBindings = enabledBindingCollectorVisitor.getEnabledBindings()
            enabledBindingCollectorVisitor.clear()
            enabledBindingCollectorVisitor.cleanStack()
            if (collectedEnabledBindings.isEmpty()) {
                break
            }

            val selectedBinding : Binding = executionConditions.selectBindingToExecute(collectedEnabledBindings)
            selectedBinding.execute(stepIndex++, logger.loggingEnabled)
            logger.logBindingExecution(selectedBinding)
            executionConditions.checkIfSuspend(this, lastExecutionBinding = selectedBinding)
        }
        logger.onEnd()

        executionLock.unlock()
    }
}
