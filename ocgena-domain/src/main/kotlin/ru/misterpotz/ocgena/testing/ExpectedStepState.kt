package ru.misterpotz.ocgena.testing

import ru.misterpotz.SimulationStepLog
import ru.misterpotz.ocgena.simulation_old.di.SimulationComponent
import ru.misterpotz.ocgena.simulation_old.stepexecutor.SparseTokenBunchImpl
import ru.misterpotz.ocgena.simulation_old.stepexecutor.TimePNTransitionMarking

data class ExpectedStepState(
    val timeClockIncrement: Long,
    val chosenTransition: String?,
    val markingApplierBlock: SparseTokenBunchImpl.Builder.() -> Unit,
    val timeMarkingApplierBlock: TimePNTransitionMarking.SettingBlock.() -> Unit
) {
    fun compareToStepLog(
        simulationStepLog: SimulationStepLog,
        simulationComponent: SimulationComponent
    ): Boolean {
        val tokenAmountStorage =
            simulationComponent.emptyTokenBunchBuilder().apply(markingApplierBlock).buildTokenBunch()
                .tokenAmountStorage.dump()
        val timePNTransitionMarking =
            simulationComponent
                .zeroClockedTransitionMarking()
                .applySettingsBlock(timeMarkingApplierBlock)
                .dump()
        return timeClockIncrement == simulationStepLog.clockIncrement &&
                chosenTransition == simulationStepLog.selectedFiredTransition?.transitionId &&
                tokenAmountStorage == simulationStepLog.endStepMarkingAmounts
//                timePNTransitionMarking == simulationStepLog.timePNTransitionMarking
    }
}
