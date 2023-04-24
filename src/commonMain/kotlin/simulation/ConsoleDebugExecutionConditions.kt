package simulation

class ConsoleDebugExecutionConditions() : ExecutionConditions {
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
    override fun checkTerminateConditionSatisfied(ocNet: RunningSimulatableOcNet): Boolean {
//        TODO("Not yet implemented")
        return false
    }

    override suspend fun checkIfSuspend(ocNet: RunningSimulatableOcNet) {

    }
}
