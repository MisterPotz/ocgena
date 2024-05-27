package ru.misterpotz.ocgena.simulation_v2.di

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.misterpotz.Logger
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.*
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.NormalShuffler
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.Shuffler
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.Transitions
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenStore
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import javax.inject.Singleton
import kotlin.random.Random

@Module
internal abstract class SimulationV2Module {
    companion object {

        @Provides
        @Singleton
        fun simulationStateAccessor(
            ocNetStruct: OCNetStruct,
            simulationInput: SimulationInput
        ): ModelAccessor {
            val modelAccessor = ModelAccessor(ocNetStruct, simulationInput)
            modelAccessor.init()
            return modelAccessor
        }

        @Provides
        @Singleton
        fun providesTokens(modelAccessor: ModelAccessor): TokenStore {
            return TokenStore(
                internalSlice = SimpleTokenSlice(
                    modelAccessor.placesRef.ref.toSortedSet(),
                ).apply {
                    for ((place, setting) in modelAccessor.simulationInput.places) {
                        if (setting.initialTokens != null) {
                            setAmount(modelAccessor.place(place), setting.initialTokens)
                        }
                    }
                }
            )
        }

        @Provides
        @Singleton
        fun providesShuffler(simulationInput: SimulationInput): Shuffler {
            return NormalShuffler(simulationInput.randomSeed?.let { Random(it) } ?: Random.Default)
        }

        @Provides
        @Singleton
        fun providesStepExecutor(
            modelAccessor: ModelAccessor,
            interactor: SimulationV2Interactor,
            tokenStore: TokenStore,
            shuffler: Shuffler,
        ): StepExecutor {
            return StepExecutor(
                transitions = modelAccessor.transitionsRef.ref,
                shiftTimeSelector = interactor,
                transitionSelector = interactor,
                tokenStore = tokenStore,
                model = modelAccessor,
                shuffler = shuffler
            )
        }

        @Provides
        @Singleton
        fun providesSimulationV2Interactor(
            shuffler: Shuffler,
            simulationV2Interactor: SimulationV2Interactor.Factory?,
        ): SimulationV2Interactor {
            return SimulationV2InteractorWrapper(shuffler = shuffler, simulationV2Interactor)
        }

        @Provides
        @Singleton
        fun simulation(
            simulationInput: SimulationInput,
            stepExecutor: StepExecutor,
            finishRequester: SimulationV2Interactor,
            logger: Logger,
        ): Simulation {
            return Simulation(
                simulationInput = simulationInput,
                stepExecutor = stepExecutor,
                logger = logger,
                finishRequestChecker = finishRequester
            )
        }

        @Provides
        @Singleton
        fun provideFinishRequester(simulationV2Interactor: SimulationV2Interactor): FinishRequestChecker {
            return simulationV2Interactor
        }
    }
}

interface SimulationV2Interactor : ShiftTimeSelector, TransitionSelector, FinishRequestChecker {
    interface Factory {
        fun create(defaultBehavior: SimulationV2Interactor): SimulationV2Interactor
    }
}

class SimulationV2InteractorWrapper(
    val shuffler: Shuffler,
    externalsimulationV2Interactor: SimulationV2Interactor.Factory?
) : SimulationV2Interactor {
    private val defaultBehavior = object : SimulationV2Interactor {
        override suspend fun get(timeRange: LongRange): Long {
            return shuffler.select(timeRange)
        }

        override suspend fun get(transitions: Transitions): TransitionWrapper {
            return shuffler.select(transitions.indices).let {
                transitions[it]
            }
        }

        override suspend fun isFinish(): Boolean {
            return false
        }
    }
    private val simulationV2Interactor = externalsimulationV2Interactor?.create(defaultBehavior)

    override suspend fun get(timeRange: LongRange): Long {
        return (simulationV2Interactor ?: defaultBehavior).get(timeRange)
    }

    override suspend fun get(transitions: Transitions): TransitionWrapper {
        return (simulationV2Interactor ?: defaultBehavior).get(transitions)
    }

    override suspend fun isFinish(): Boolean {
        return (simulationV2Interactor ?: defaultBehavior).isFinish()
    }
}

@Component(
    modules = [SimulationV2Module::class]
)
@Singleton
interface SimulationV2Component {
    fun stepExecutor(): StepExecutor
    fun simulation(): Simulation
    fun model(): ModelAccessor
    fun ocnet(): OCNetStruct
    fun tokenstore(): TokenStore
    fun simulationInput() : SimulationInput
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance simulationInput: SimulationInput,
            @BindsInstance ocNetStruct: OCNetStruct,
            @BindsInstance simulationV2Interactor: SimulationV2Interactor.Factory?,
            @BindsInstance logger: Logger
        ): SimulationV2Component
    }

    companion object {
        fun create(
            simulationInput: SimulationInput,
            ocNetStruct: OCNetStruct,
            simulationV2Interactor: SimulationV2Interactor.Factory?,
            logger: Logger,
        ): SimulationV2Component {
            return DaggerSimulationV2Component.factory()
                .create(
                    simulationInput,
                    ocNetStruct,
                    simulationV2Interactor,
                    logger
                )
        }
    }
}