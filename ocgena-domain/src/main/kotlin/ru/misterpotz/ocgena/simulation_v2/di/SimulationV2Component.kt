package ru.misterpotz.ocgena.simulation_v2.di

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.*
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.Shuffler
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenStore
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import kotlin.random.Random

@Module
abstract class SimulationV2Module {
    companion object {

        @Provides
        fun simulationStateAccessor(
            ocNetStruct: OCNetStruct,
            simulationInput: SimulationInput
        ): ModelAccessor {
            val modelAccessor = ModelAccessor(ocNetStruct, simulationInput)
            modelAccessor.init()
            return modelAccessor
        }

        @Provides
        fun providesTokens(modelAccessor: ModelAccessor): TokenStore {
            return TokenStore(
                internalSlice = SimpleTokenSlice(
                    modelAccessor.placesRef.ref.toMutableSet(),
                ),
                modelAccessor = modelAccessor
            )
        }

        @Provides
        fun providesShuffler(simulationInput: SimulationInput): Shuffler {
            return NormalShuffler(simulationInput.randomSeed?.let { Random(it) } ?: Random.Default)
        }

        @Provides
        fun providesStepExecutor(
            modelAccessor: ModelAccessor,
            interactor: SimulationV2Interactor,
            tokenStore: TokenStore,
            shuffler: Shuffler,
        ): StepExecutor {
            return StepExecutor(
                transitions = modelAccessor.transitionsRef.ref,
                places = modelAccessor.placesRef.ref,
                shiftTimeSelector = interactor,
                transitionSelector = interactor,
                transitionSolutionSelector = interactor,
                tokenStore = tokenStore,
                model = modelAccessor,
                shuffler = shuffler
            )
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
        fun create(
            simulationInput: SimulationInput,
            ocNetStruct: OCNetStruct,
            simulationV2Interactor: SimulationV2Interactor
        ): SimulationV2Component {
            return DaggerSimulationV2Component.factory()
                .create(
                    simulationInput,
                    ocNetStruct,
                    simulationV2Interactor
                )
        }
    }
}