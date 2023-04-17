package simulation

import error.prettyPrint
import model.Binding
import model.WellFormedOCNet
import utils.mprintln

class ConsoleDebugExecutionConditions() : ExecutionConditions {
    override fun checkTerminateConditionSatisfied(ocNet: WellFormedOCNet): Boolean {
        // TODO: check for terminate token amounts at place nodes
        return false
    }

    override suspend fun checkIfSuspend(ocNet: WellFormedOCNet, lastExecutionBinding: Binding) {
        Unit
    }

    override suspend fun selectBindingToExecute(enabledBindings: List<Binding>): Binding {
        mprintln("\tenabled bindings: \n${enabledBindings.prettyPrint().prependIndent()}")
        // by default, select the first one
        return enabledBindings.first()
    }
}