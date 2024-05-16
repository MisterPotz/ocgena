package ru.misterpotz.ocgena.simulation_old

import ru.misterpotz.ocgena.simulation_old.structure.SimulatableOcNetInstance


interface ExecutionConditions {

    // for terminate
    fun checkTerminateConditionSatisfied(ocNet: SimulatableOcNetInstance): Boolean

    // TODO: for debug ability
//    fun checkIfSuspend(ocNet: RunningSimulatableOcNet)

    // TODO: this method can fulfill both interactive mode and automatic
//    suspend fun selectBindingToExecute(enabledBindings: List<EnabledSimpleBinding>): EnabledSimpleBinding
}

