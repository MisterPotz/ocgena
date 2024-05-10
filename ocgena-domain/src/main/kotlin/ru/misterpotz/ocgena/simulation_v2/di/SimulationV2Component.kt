package ru.misterpotz.ocgena.simulation_v2.di

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunchImpl
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.ShiftTimeSelector
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.StepExecutor
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.TransitionSelector
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.TransitionSolutionSelector
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput

@Module
abstract class SimulationV2Module {
    companion object {
        @Provides
        fun providesTokenBunch() : SparseTokenBunch {
            return SparseTokenBunchImpl()
        }
    }
}

interface SimulationV2Interactor : ShiftTimeSelector, TransitionSelector, TransitionSolutionSelector

@Component(modules = [SimulationV2Module::class])
interface SimulationV2Component {
    fun stepExecutor(): StepExecutor

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance simulationInput: SimulationInput,
            @BindsInstance ocNetStruct: OCNetStruct,
            @BindsInstance simulationV2Interactor: SimulationV2Interactor
        ): SimulationV2Component
    }

    companion object {
        fun create() {
            return DaggerSimulationV2Component.factory()
                .create(
                    simulationInput,
                    ocNetStruct,
                    simulationV2Interactor
                )
        }
    }
}