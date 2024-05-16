package ru.misterpotz.ocgena.simulation_old

import ru.misterpotz.ocgena.simulation_old.structure.SimulatableOcNetInstance

class SimpleExecutionConditions() : ExecutionConditions {
    //    override fun checkTerminateConditionSatisfied(ocNet: StaticCoreOcNet): Boolean {
//        // TODO: check for terminate token amounts at place nodes
//        return false
//    }
//
//    override suspend fun checkIfSuspend(ocNet: StaticCoreOcNet, lastExecutionBinding: EnabledSimpleBinding) {
//        Unit
//    }
//
//    override suspend fun selectBindingToExecute(enabledBindings: List<EnabledSimpleBinding>): EnabledSimpleBinding {
//        mprintln("\tenabled bindings: \n${enabledBindings.prettyPrint().prependIndent()}")
//        // by default, select the first one
//        return enabledBindings.first()
//    }
    override fun checkTerminateConditionSatisfied(ocNet: SimulatableOcNetInstance): Boolean {
//        TODO("Not yet implemented")
        return false
    }
}

