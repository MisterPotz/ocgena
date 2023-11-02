package ru.misterpotz.ocgena.simulation.di

import dagger.*
import kotlinx.serialization.json.Json
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import net.mamoe.yamlkt.Yaml
import ru.misterpotz.marking.objects.ObjectTokenSet
import ru.misterpotz.marking.objects.ObjectTokenSetMap
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.api.interactors.TIOutputPlacesResolverInteractor
import ru.misterpotz.simulation.api.interactors.BindingSelectionInteractor
import ru.misterpotz.simulation.api.interactors.TokenSelectionInteractor
import ru.misterpotz.simulation.impl.interactors.BindingSelectionInteractorImpl
import ru.misterpotz.simulation.impl.interactors.TokenSelectionInteractorImpl
import ru.misterpotz.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.simulation.logging.LogConfiguration
import ru.misterpotz.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.simulation.logging.loggers.CurrentSimulationDelegateImpl
import ru.misterpotz.simulation.logging.loggers.FullLoggerFactory
import ru.misterpotz.simulation.logging.loggers.StepAggregatingLogReceiver
import ru.misterpotz.simulation.queue.GenerationQueue
import ru.misterpotz.simulation.queue.GenerationQueueFactory
import ru.misterpotz.simulation.queue.GenerationQueueFactoryImpl
import ru.misterpotz.simulation.transition.TransitionInstanceDurationGenerator
import ru.misterpotz.simulation.transition.TransitionInstanceNextCreationTimeGenerator
import simulation.*
import simulation.binding.*
import simulation.random.*
import javax.inject.Scope
import kotlin.random.Random


@Module
internal abstract class SimulationModule {
    @Binds
    @SimulationScope
    abstract fun tokenSelector(tokenSelector: TokenSelectionInteractorImpl): TokenSelectionInteractor

    @Binds
    @SimulationScope
    abstract fun bindingSelector(bindingSelector: BindingSelectionInteractorImpl): BindingSelectionInteractor

    @Provides
    @SimulationScope
    fun executionConditions(): ExecutionConditions {
        return SimpleExecutionConditions()
    }

    @Provides
    @SimulationScope
    fun logger(fullLoggerFactory: FullLoggerFactory) : Logger {
        return fullLoggerFactory.createLogger()
    }

    @Provides
    @SimulationScope
    fun random(randomFactory: RandomFactoryImpl): Random {
        return randomFactory.create()
    }

    @Provides
    @SimulationScope
    fun labelMapping(simulationConfig: SimulationConfig): NodeToLabelRegistry {
        return simulationConfig.labelMapping
    }

    @Binds
    @SimulationScope
    abstract fun simulationStateProvider(simulationStateProviderImpl: SimulationStateProviderImpl):
            SimulationStateProvider

    @Binds
    @SimulationScope
    abstract fun currentSimulationDelegate(currentSimulationDelegate: CurrentSimulationDelegateImpl): CurrentSimulationDelegate

    @Provides
    @SimulationScope
    fun objectTokenMover(objectTokenMoverFactory: ObjectTokenMoverFactoryImpl) : LockedTokensMover {
        return objectTokenMoverFactory.create()
    }

    @Provides
    @SimulationScope
    fun provideTransitionDurationSelector(
        random: Random,
        simulationConfig: SimulationConfig
    ): TransitionInstanceDurationGenerator {
        return TransitionInstanceDurationGenerator(
            random,
            intervalFunction = simulationConfig.templateOcNet.intervalFunction
        )
    }

    @Provides
    @SimulationScope
    fun transitionInstanceOccurrenceDeltaSelector(
        random: Random,
        simulationConfig: SimulationConfig
    ): TransitionInstanceNextCreationTimeGenerator {
        return TransitionInstanceNextCreationTimeGenerator(
            random,
            intervalFunction = simulationConfig.templateOcNet.intervalFunction
        )
    }

    @Provides
    @SimulationScope
    fun ocNetType(simulationConfig: SimulationConfig): OcNetType {
        return simulationConfig.ocNetType
    }

    @Binds
    @SimulationScope
    abstract fun objectTokenMoverFactory(objectTokenMoverFactory: ObjectTokenMoverFactoryImpl): ObjectTokenMoverFactory

    @Provides
    @SimulationScope
    abstract fun bindingOutputMarkingResolverFactory(factory: BindingOutputMarkingResolverFactoryImpl):
            BindingOutputMarkingResolverFactory

    @Provides
    @SimulationScope
    fun bindingOutputMarkingResolver(bindingOutputMarkingResolverFactory: BindingOutputMarkingResolverFactory): TIOutputPlacesResolverInteractor {
        return bindingOutputMarkingResolverFactory.create()
    }

    @Binds
    @SimulationScope
    abstract fun generationQueueFactory(
        simulationConfig: GenerationQueueFactoryImpl
    ): GenerationQueueFactory

    @Provides
    @SimulationScope
    fun generationQueue(
        simulationConfig: SimulationConfig,
        generationQueueFactory: GenerationQueueFactory
    ): GenerationQueue {
        return generationQueueFactory.createGenerationQueue(simulationConfig)
    }

    @Binds
    @SimulationScope
    abstract fun activeTransitionFinisher(transitionInstanceFinisherImpl: TIFinisherImpl):
            TIFinisher

    @Binds
    @SimulationScope
    abstract fun objectTokenSet(objectTokenSet: ObjectTokenSetMap): ObjectTokenSet
}

interface SimulationComponentDependencies {
    val json: Json
    val yaml : Yaml
}

@SimulationScope
@Component(
    modules = [SimulationModule::class],
    dependencies = [SimulationComponentDependencies::class]
)
interface SimulationComponent {
    fun simulationTask(): SimulationTask

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance simulationParams: SimulationConfig,
            @BindsInstance loggingConfiguration: LogConfiguration,
            @BindsInstance randomFactory: RandomFactory,
            @BindsInstance developmentDebugConfig: DevelopmentDebugConfig,
            @BindsInstance stepAggregatingLogReceiver: StepAggregatingLogReceiver
        ): SimulationComponent
    }
}

@Scope
annotation class SimulationScope