package ru.misterpotz.simulation.di

import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.transition.TransitionDurationSelector
import ru.misterpotz.simulation.transition.TransitionInstanceOccurenceDeltaSelector
import simulation.*
import simulation.random.*
import javax.inject.Scope
import kotlin.random.Random


@Module
abstract class SimulationModule {
    @Binds
    @SimulationScope
    abstract fun tokenSelector(tokenSelector: TokenSelectorImpl): TokenSelector

    @Binds
    @SimulationScope
    abstract fun bindingSelector(bindingSelector: BindingSelectorImpl) : BindingSelector

    @Provides
    @SimulationScope
    fun executionConditions() : ExecutionConditions {
        return SimpleExecutionConditions()
    }

    @Provides
    @SimulationScope
    fun random(randomFactory: RandomFactory): Random {
        return randomFactory.create()
    }
}

interface SimulationComponentDependencies {}

@SimulationScope
@Component(
    modules = [SimulationModule::class],
    dependencies = [SimulationComponentDependencies::class]
)
interface SimulationComponent {
    fun simulationTaskExecutor(): SimulationTaskStepExecutor



    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            simulationParams: SimulationConfig,
            @BindsInstance logger: Logger,
            @BindsInstance randomFactory: RandomFactory,
            transitionDurationSelector: TransitionDurationSelector,
            transitionInstanceOccurenceDeltaSelector: TransitionInstanceOccurenceDeltaSelector,
            tokenNextTimeSelector: TokenGenerationTimeSelector,
            dumpState: Boolean = false,
        ): SimulationComponent
    }
}

@Scope
annotation class SimulationScope