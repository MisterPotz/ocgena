package ru.misterpotz.ocgena.simulation

import ru.misterpotz.ocgena.simulation.structure.SimulationableOcNetInstance


interface ExecutionConditions {

    // for terminate
    fun checkTerminateConditionSatisfied(ocNet: SimulationableOcNetInstance): Boolean

    // TODO: for debug ability
//    fun checkIfSuspend(ocNet: RunningSimulatableOcNet)

    // TODO: this method can fulfill both interactive mode and automatic
//    suspend fun selectBindingToExecute(enabledBindings: List<EnabledSimpleBinding>): EnabledSimpleBinding
}

