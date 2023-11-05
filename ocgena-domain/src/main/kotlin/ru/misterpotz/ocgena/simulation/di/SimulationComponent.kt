package ru.misterpotz.ocgena.simulation.di

import com.charleskorn.kaml.Yaml
import dagger.*
import kotlinx.serialization.json.Json
import ru.misterpotz.ocgena.collections.ObjectTokenSet
import ru.misterpotz.ocgena.collections.ObjectTokenSetMap
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType
import ru.misterpotz.ocgena.registries.ArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistryDelegating
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.registries.delegates.CompoundArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityNormalDelegateTypeA
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityVariableDelegateTypeA
import ru.misterpotz.ocgena.simulation.*
import ru.misterpotz.ocgena.simulation.binding.TIFinisher
import ru.misterpotz.ocgena.simulation.binding.TIFinisherImpl
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.generator.*
import ru.misterpotz.ocgena.simulation.interactors.*
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.FullLoggerFactory
import ru.misterpotz.ocgena.simulation.logging.LogConfiguration
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegateImpl
import ru.misterpotz.ocgena.simulation.logging.loggers.StepAggregatingLogReceiver
import ru.misterpotz.ocgena.simulation.state.PMarkingProvider
import ru.misterpotz.ocgena.simulation.state.StateImpl
import ru.misterpotz.ocgena.simulation.structure.SimulatableOcNetInstance
import ru.misterpotz.ocgena.simulation.structure.SimulatableOcNetInstanceImpl
import ru.misterpotz.ocgena.simulation.structure.State
import ru.misterpotz.ocgena.simulation.token_generation.ObjectTokenGenerator
import simulation.Logger
import simulation.binding.BindingOutputMarkingResolverFactory
import simulation.binding.BindingOutputMarkingResolverFactoryImpl
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

    @Binds
    @SimulationScope
    abstract fun enabledBindingResolverInteractor(
        enabledBindingResolverInteractorImpl: EnabledBindingResolverInteractorImpl
    ): EnabledBindingResolverInteractor

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
                transitionInstancesTimesSpec = simulationConfig.transitionInstancesTimesSpec
            )
        }

        @Provides
        @SimulationScope
        fun objectTokenSet(): ObjectTokenSet {
            return ObjectTokenSetMap(mutableMapOf())
        }

        @Provides
        @SimulationScope
        fun ocNetInstance(ocNet: OCNet, state: State, ocNetType: OcNetType): SimulatableOcNetInstance {
            return SimulatableOcNetInstanceImpl(
                ocNet = ocNet,
                state = state,
                ocNetType = ocNetType
            )
        }

        @Provides
        @SimulationScope
        fun objectTokenGenerator(): ObjectTokenGenerator {
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
                transitionInstancesTimesSpec = simulationConfig.transitionInstancesTimesSpec
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

        @Provides
        @SimulationScope
        fun ocNet(simulationConfig: SimulationConfig): OCNet {
            return simulationConfig.ocNet
        }

        @Provides
        @SimulationScope
        fun arcMultiplicityRegistry(
            ocNet: OCNet,
            arcsMultiplicityDelegate: ArcsMultiplicityDelegate
        ): ArcsMultiplicityRegistry {
            return ArcsMultiplicityRegistryDelegating(
                petriAtomRegistry = ocNet.petriAtomRegistry,
                arcsRegistry = ocNet.arcsRegistry,
                arcsMultiplicityDelegate = arcsMultiplicityDelegate
            )
        }

        @Provides
        @SimulationScope
        fun arcMultiplicityDelegate(pMarkingProvider: PMarkingProvider): ArcsMultiplicityDelegate {
            return CompoundArcsMultiplicityDelegate(
                arcMultiplicityDelegates = buildMap {
                    put(
                        ArcType.NORMAL,
                        ArcToMultiplicityNormalDelegateTypeA(pMarkingProvider)
                    )
                    put(
                        ArcType.VARIABLE,
                        ArcToMultiplicityVariableDelegateTypeA(pMarkingProvider)
                    )
                }
            )
        }

        @Provides
        @SimulationScope
        fun state(
            ocNet: OCNet,
            arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
            pMarkingProvider: PMarkingProvider,
        ): State {
            return StateImpl(
                ocNet = ocNet,
                arcsMultiplicityRegistry = arcsMultiplicityRegistry,
                pMarkingProvider = pMarkingProvider
            )
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