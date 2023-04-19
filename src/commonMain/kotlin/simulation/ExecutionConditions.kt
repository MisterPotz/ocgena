package simulation

import model.ActiveBinding
import model.WellFormedOCNet

interface ExecutionConditions {

    // for terminate
    fun checkTerminateConditionSatisfied(ocNet: WellFormedOCNet): Boolean

    // TODO: for debug ability
    suspend fun checkIfSuspend(ocNet: WellFormedOCNet, lastExecutionBinding: ActiveBinding)

    // TODO: this method can fulfill both interactive mode and automatic
    suspend fun selectBindingToExecute(enabledBindings: List<ActiveBinding>): ActiveBinding
}
