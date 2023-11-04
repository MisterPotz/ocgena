package ru.misterpotz.ocgena.simulation.di

import dagger.*
import kotlinx.serialization.json.Json
import net.mamoe.yamlkt.Yaml
import ru.misterpotz.ocgena.collections.objects.ObjectTokenSet
import ru.misterpotz.ocgena.collections.objects.ObjectTokenSetMap
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.*
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.generator.*
import ru.misterpotz.ocgena.simulation.interactors.*
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.LogConfiguration
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegateImpl
import ru.misterpotz.ocgena.simulation.logging.FullLoggerFactory
import ru.misterpotz.ocgena.simulation.logging.loggers.StepAggregatingLogReceiver
import simulation.*
import simulation.binding.BindingOutputMarkingResolverFactory
import simulation.binding.BindingOutputMarkingResolverFactoryImpl
import ru.misterpotz.ocgena.simulation.binding.TIFinisher
import ru.misterpotz.ocgena.simulation.binding.TIFinisherImpl
import ru.misterpotz.ocgena.simulation.structure.OcNetInstance
import ru.misterpotz.ocgena.simulation.token_generation.ObjectTokenGenerator
import simulation.random.RandomFactory
import simulation.random.RandomFactoryImpl
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

    @Binds
    @SimulationScope
    abstract fun simulationStateProvider(simulationStateProviderImpl: SimulationStateProviderImpl):
            SimulationStateProvider

    @Binds
    @SimulationScope
    abstract fun currentSimulationDelegate(currentSimulationDelegate: CurrentSimulationDelegateImpl): CurrentSimulationDelegate


    @Binds
    @SimulationScope
    abstract fun objectTokenMoverFactory(objectTokenMoverFactory: ObjectTokenMoverFactoryImpl): ObjectTokenMoverFactory

    @Binds
    @SimulationScope
    abstract fun generationQueueFactory(
        simulationConfig: NewTokenTimeBasedGeneratorFactoryImpl
    ): NewTokenTimeBasedGeneratorFactory

    @Binds
    @SimulationScope
    abstract fun activeTransitionFinisher(transitionInstanceFinisherImpl: TIFinisherImpl):
            TIFinisher

    @Binds
    @SimulationScope
    abstract fun bindingOutputMarkingResolverFactory(factory: BindingOutputMarkingResolverFactoryImpl):
            BindingOutputMarkingResolverFactory

    companion object {
        @Provides
        @SimulationScope
        fun logger(fullLoggerFactory: FullLoggerFactory): Logger {
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
            return simulationConfig.nodeToLabelRegistry
        }

        @Provides
        @SimulationScope
        fun objectTokenMover(objectTokenMoverFactory: ObjectTokenMoverFactoryImpl): LockedTokensMover {
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
                intervalFunction = simulationConfig.ocNetInstance.intervalFunction
            )
        }
        @Provides
        @SimulationScope
        fun objectTokenSet(): ObjectTokenSet {
            return ObjectTokenSetMap(mutableMapOf())
        }

        @Provides
        @SimulationScope
        fun ocNetInstance(simulationConfig: SimulationConfig) : OcNetInstance {
            return simulationConfig.ocNetInstance
        }

        @Provides
        @SimulationScope
        fun objectTokenGenerator() : ObjectTokenGenerator {
            return ObjectTokenGenerator()
        }

        @Provides
        @SimulationScope
        fun transitionInstanceOccurrenceDeltaSelector(
            random: Random,
            simulationConfig: SimulationConfig
        ): TransitionNextInstanceAllowedTimeGenerator {
            return TransitionNextInstanceAllowedTimeGenerator(
                random,
                intervalFunction = simulationConfig.ocNetInstance.intervalFunction
            )
        }

        @Provides
        @SimulationScope
        fun ocNetType(simulationConfig: SimulationConfig): OcNetType {
            return simulationConfig.ocNetType
        }

        @Provides
        @SimulationScope
        fun bindingOutputMarkingResolver(bindingOutputMarkingResolverFactory: BindingOutputMarkingResolverFactory): TIOutputPlacesResolverInteractor {
            return bindingOutputMarkingResolverFactory.create()
        }

        @Provides
        @SimulationScope
        fun generationQueue(
            simulationConfig: SimulationConfig,
            newTokenTimeBasedGeneratorFactory: NewTokenTimeBasedGeneratorFactory
        ): NewTokenTimeBasedGenerator {
            return newTokenTimeBasedGeneratorFactory.createGenerationQueue(simulationConfig)
        }

        @Provides
        @SimulationScope
        fun executionConditions(): ExecutionConditions {
            return SimpleExecutionConditions()
        }
    }
}

interface SimulationComponentDependencies {
    val json: Json
    val yaml: Yaml
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
            @BindsInstance stepAggregatingLogReceiver: StepAggregatingLogReceiver,
            componentDependencies: SimulationComponentDependencies,
        ): SimulationComponent
    }
}

@Scope
annotation class SimulationScope