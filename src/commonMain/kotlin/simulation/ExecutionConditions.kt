package simulation

import model.Binding
import model.WellFormedOCNet

interface ExecutionConditions {

    // for terminate
    fun checkTerminateConditionSatisfied(ocNet: WellFormedOCNet): Boolean

    // TODO: for debug ability
    suspend fun checkIfSuspend(ocNet: WellFormedOCNet, lastExecutionBinding: Binding)

    // TODO: this method can fulfill both interactive mode and automatic
    suspend fun selectBindingToExecute(enabledBindings: List<Binding>): Binding
}
