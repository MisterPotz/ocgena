package ru.misterpotz.ocgena.simulation.di

import com.charleskorn.kaml.Yaml
import dagger.*
import kotlinx.serialization.json.Json
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistryImpl
import ru.misterpotz.ocgena.collections.ObjectTokenSet
import ru.misterpotz.ocgena.collections.ObjectTokenSetMap
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType.AALST
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType.LOMAZOVA
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.registries.delegates.CompoundArcsMultiplicityDelegate
import ru.misterpotz.ocgena.registries.original.TransitionToInstancesRegistryOriginal
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityNormalDelegateTypeA
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityVariableDelegateTypeA
import ru.misterpotz.ocgena.registries.typel.ArcToMultiplicityVariableDelegateTypeL
import ru.misterpotz.ocgena.simulation.*
import ru.misterpotz.ocgena.simulation.binding.TIFinisher
import ru.misterpotz.ocgena.simulation.binding.TIFinisherImpl
import ru.misterpotz.ocgena.simulation.binding.buffer.TransitionGroupedTokenInfo
import ru.misterpotz.ocgena.simulation.binding.consumer.OutputTokensBufferConsumerFactory
import ru.misterpotz.ocgena.simulation.binding.generator.OutputMissingTokensGeneratorFactory
import ru.misterpotz.ocgena.simulation.binding.groupstrat.ByObjTypeAndArcGroupingStrategy
import ru.misterpotz.ocgena.simulation.binding.groupstrat.ByObjTypeGroupingStrategy
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation.continuation.NoOpExecutionContinuation
import ru.misterpotz.ocgena.simulation.generator.*
import ru.misterpotz.ocgena.simulation.generator.original.TransitionInstanceDurationGeneratorOriginal
import ru.misterpotz.ocgena.simulation.generator.original.TransitionNextInstanceAllowedTimeGeneratorOriginal
import ru.misterpotz.ocgena.simulation.interactors.*
import ru.misterpotz.ocgena.simulation.interactors.EnabledBindingResolverInteractor
import ru.misterpotz.ocgena.simulation.interactors.original.EnabledBindingResolverInteractorOriginalImpl
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.FullLoggerFactory
import ru.misterpotz.ocgena.simulation.logging.LogConfiguration
import ru.misterpotz.ocgena.simulation.logging.Logger
import ru.misterpotz.ocgena.simulation.state.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.state.CurrentSimulationDelegateImpl
import ru.misterpotz.ocgena.simulation.logging.loggers.NoOpStepAggregatingLogReceiver
import ru.misterpotz.ocgena.simulation.logging.loggers.StepAggregatingLogReceiver
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation.state.PMarkingProvider
import ru.misterpotz.ocgena.simulation.state.StateImpl
import ru.misterpotz.ocgena.simulation.state.original.CurrentSimulationStateOriginal
import ru.misterpotz.ocgena.simulation.stepexecutor.GlobalSparseTokenBunch
import ru.misterpotz.ocgena.simulation.stepexecutor.OriginalStepExecutor
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation.stepexecutor.StepExecutor
import ru.misterpotz.ocgena.simulation.structure.SimulatableOcNetInstance
import ru.misterpotz.ocgena.simulation.structure.SimulatableOcNetInstanceImpl
import ru.misterpotz.ocgena.simulation.structure.State
import ru.misterpotz.ocgena.simulation.token_generation.ObjectTokenGenerator
import ru.misterpotz.ocgena.simulation.transition.TransitionInstanceCreatorFacadeOriginal
import simulation.binding.BindingOutputMarkingResolverFactory
import simulation.binding.BindingOutputMarkingResolverFactoryImpl
import simulation.random.RandomFactoryImpl
import java.lang.IllegalStateException
import javax.inject.Provider
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
    abstract fun generationQueueFactory(
        simulationConfig: NewTokenTimeBasedGeneratorFactoryImpl,
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
        enabledBindingResolverInteractorOriginalImpl: EnabledBindingResolverInteractorOriginalImpl,
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
        fun provideTransitionDurationSelector(
            random: Random,
            simulationConfig: SimulationConfig,
        ): TransitionInstanceDurationGeneratorOriginal {
            return TransitionInstanceDurationGeneratorOriginal(
                random,
                transitionsOriginalSpec = simulationConfig.castTransitions()
            )
        }

        @Provides
        @SimulationScope
        fun providesObjectRealTokenAmountRegistry(ocNet: OCNet): ObjectTokenRealAmountRegistry {
            return ObjectTokenRealAmountRegistryImpl(
                ocNet.placeToObjectTypeRegistry
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
        fun objectTokenGenerator(objectTokenSet: ObjectTokenSet): ObjectTokenGenerator {
            return ObjectTokenGenerator(objectTokenSet)
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
            newTokenTimeBasedGeneratorFactory: NewTokenTimeBasedGeneratorFactory,
        ): NewTokenTimeBasedGenerator {
            return newTokenTimeBasedGeneratorFactory.createGenerationQueue(simulationConfig)
        }

        @Provides
        @SimulationScope
        fun provideSimulationTaskPreparatorOriginal(
            simulationConfig: SimulationConfig,
            objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
            transitionNextInstanceAllowedTimeGeneratorOriginal: TransitionNextInstanceAllowedTimeGeneratorOriginal,
            newTokenTimeBasedGenerator: NewTokenTimeBasedGenerator,
            transitionToInstancesRegistryOriginal: TransitionToInstancesRegistryOriginal,
            currentSimulationStateOriginal: CurrentSimulationStateOriginal,
        ): SimulationTaskPreparatorOriginal {
            return SimulationTaskPreparatorOriginal(
                simulationConfig = simulationConfig,
                objectTokenRealAmountRegistry = objectTokenRealAmountRegistry,
                activityAllowedTimeSelector = transitionNextInstanceAllowedTimeGeneratorOriginal,
                newTokenTimeBasedGenerator = newTokenTimeBasedGenerator,
                transitionToInstancesRegistry = transitionToInstancesRegistryOriginal,
                currentSimulationStateOriginal = currentSimulationStateOriginal
            )
        }

        @Provides
        @SimulationScope
        fun provideTransitionToInstancesRegistryOriginal(): TransitionToInstancesRegistryOriginal {
            return TransitionToInstancesRegistryOriginal()
        }

        @Provides
        @SimulationScope
        fun provideSimulationTaskPreparator(
            simulationConfig: SimulationConfig,
            simulationTaskPreparatorProvider: Provider<SimulationTaskPreparatorOriginal>,
        ): SimulationTaskPreparator {
            val simulationTaskPreparator = when (simulationConfig.simulationSemantics.type) {
                SimulationSemanticsType.ORIGINAL -> {
                    require(simulationConfig.transitionsSpec is TransitionsOriginalSpec)
                    val new = simulationTaskPreparatorProvider.get()
                    require(new is SimulationTaskPreparatorOriginal)
                    new
                }

                SimulationSemanticsType.SIMPLE_TIME_PN -> {
                    throw IllegalStateException()
                }
            }
            return simulationTaskPreparator
        }

        @Provides
        @SimulationScope
        fun provideTransitionNextInstanceAllowedTimeGenerator(
            random: Random,
            simulationConfig: SimulationConfig,
        ): TransitionNextInstanceAllowedTimeGeneratorOriginal {
            return TransitionNextInstanceAllowedTimeGeneratorOriginal(
                random = random,
                transitionsOriginalSpec = simulationConfig.castTransitions()
            )
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
            arcsMultiplicityDelegate: ArcsMultiplicityDelegate,
        ): ArcsMultiplicityRegistry {
            return ArcsMultiplicityRegistryDelegating(
                arcsRegistry = ocNet.arcsRegistry,
                arcsMultiplicityDelegate = arcsMultiplicityDelegate
            )
        }

        @Provides
        @SimulationScope
        fun arcPrePlaceHasEnoughTokensChecker(
            arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
        ): ArcPrePlaceHasEnoughTokensChecker {
            return ArcPrePlaceHasEnoughTokensCheckerImpl(arcsMultiplicityRegistry)
        }

        @Provides
        @SimulationScope
        fun providesCurrentSimulationStateOriginal() = CurrentSimulationStateOriginal()


        @Provides
        @SimulationScope
        fun provideStepExecutor(
            simulationConfig: SimulationConfig,
            simulationStateProvider: SimulationStateProvider,
            bindingSelectionInteractor: BindingSelectionInteractor,
            tiFinisher: TIFinisher,
            transitionToInstancesRegistryOriginal: Provider<TransitionInstanceCreatorFacadeOriginal>,
            logger: Logger,
            newTokenTimeBasedGenerator: NewTokenTimeBasedGenerator,
            enabledBindingsCollectorInteractor: EnabledBindingsCollectorInteractor,
            objectTokenSet: ObjectTokenSet,
            objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
            currentSimulationStateOriginal: Provider<CurrentSimulationStateOriginal>,
        ): StepExecutor {
            return when (simulationConfig.simulationSemantics.type) {
                SimulationSemanticsType.ORIGINAL -> {
                    OriginalStepExecutor(
                        simulationStateProvider,
                        bindingSelectionInteractor = bindingSelectionInteractor,
                        transitionFinisher = tiFinisher,
                        transitionInstanceCreatorFacadeOriginal = transitionToInstancesRegistryOriginal.get(),
                        logger = logger,
                        newTokenTimeBasedGenerator = newTokenTimeBasedGenerator,
                        bindingsCollector = enabledBindingsCollectorInteractor,
                        objectTokenSet = objectTokenSet,
                        objectTokenRealAmountRegistry = objectTokenRealAmountRegistry,
                        currentSimulationStateOriginal = currentSimulationStateOriginal.get()
                    )
                }

                SimulationSemanticsType.SIMPLE_TIME_PN -> {
                    throw IllegalStateException()
                }
            }
        }

        @Provides
        @SimulationScope
        fun arcMultiplicityDelegate(
            arcToMultiplicityNormalDelegateTypeA: ArcToMultiplicityNormalDelegateTypeA,
            arcToMultiplicityVariableDelegateTypeA: ArcToMultiplicityVariableDelegateTypeA,
            arcToMultiplicityVariableDelegateTypeL: ArcToMultiplicityVariableDelegateTypeL,
            ocNetType: OcNetType,
        ): ArcsMultiplicityDelegate {
            return CompoundArcsMultiplicityDelegate(
                arcMultiplicityDelegates = buildMap {
                    put(
                        ArcType.NORMAL,
                        arcToMultiplicityNormalDelegateTypeA
                    )
                    when (ocNetType) {
                        AALST -> {
                            put(
                                ArcType.VARIABLE,
                                arcToMultiplicityVariableDelegateTypeA
                            )
                        }

                        LOMAZOVA -> {
                            put(
                                ArcType.VARIABLE,
                                arcToMultiplicityVariableDelegateTypeL
                            )
                        }
                    }
                }
            )
        }

        @Provides
        @SimulationScope
        fun prePlaceRegistry(
            ocNet: OCNet,
            arcPrePlaceHasEnoughTokensChecker: ArcPrePlaceHasEnoughTokensChecker,
        ): PrePlaceRegistry {
            return PrePlaceRegistryImpl.create(
                ocNet,
                arcPrePlaceHasEnoughTokensChecker = arcPrePlaceHasEnoughTokensChecker
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

        @Provides
        @SimulationScope
        fun tokenBatchGroupingStrategy(
            byObjTypeGroupingStrategy: ByObjTypeGroupingStrategy,
            byObjTypeAndArcGroupingStrategy: ByObjTypeAndArcGroupingStrategy,
        ): TransitionGroupedTokenInfo.TokenGroupingStrategy {
            return byObjTypeGroupingStrategy
        }

        @Provides
        @GlobalTokenBunch
        @SimulationScope
        fun globalTokenBunch(
            pMarkingProvider: PMarkingProvider,
            objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
        ): SparseTokenBunch {
            return GlobalSparseTokenBunch(
                pMarkingProvider,
                objectTokenRealAmountRegistry
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
    fun simulationConfig(): SimulationConfig
    fun state(): State
    fun ocNet(): OCNet
    fun enabledBindingsResolver(): EnabledBindingResolverInteractor
    fun objectTokenRealAmountRegistry(): ObjectTokenRealAmountRegistry
    fun outputTokensBufferConsumerFactory(): OutputTokensBufferConsumerFactory
    fun outputMissingTokensGeneratorFactory(): OutputMissingTokensGeneratorFactory
    fun batchGroupingStrategy(): TransitionGroupedTokenInfo.TokenGroupingStrategy
    fun objectTokenSet(): ObjectTokenSet
    fun objectTokenGenerator(): ObjectTokenGenerator
    fun newTokenGenerationFacade(): NewTokenGenerationFacade
    fun tokenSelectionInteractor(): TokenSelectionInteractor
    fun prePostPlaceRegistry(): PrePlaceRegistry
    fun arcPrePlaceHasEnoughTokensChecker(): ArcPrePlaceHasEnoughTokensChecker

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance simulationParams: SimulationConfig,
            @BindsInstance loggingConfiguration: LogConfiguration,
            @BindsInstance developmentDebugConfig: DevelopmentDebugConfig,
            @BindsInstance stepAggregatingLogReceiver: StepAggregatingLogReceiver,
            @BindsInstance
            executionContinuation: ExecutionContinuation,
            componentDependencies: SimulationComponentDependencies,
        ): SimulationComponent
    }

    companion object {
        fun defaultCreate(
            simulationConfig: SimulationConfig,
            componentDependencies: SimulationComponentDependencies,
            developmentDebugConfig: DevelopmentDebugConfig,
            executionContinuation: ExecutionContinuation = NoOpExecutionContinuation(),
        ): SimulationComponent {
            return create(
                simulationParams = simulationConfig,
                loggingConfiguration = LogConfiguration.default(),
                developmentDebugConfig = developmentDebugConfig,
                stepAggregatingLogReceiver = NoOpStepAggregatingLogReceiver,
                componentDependencies = componentDependencies,
                executionContinuation = executionContinuation
            )
        }

        fun create(
            simulationParams: SimulationConfig,
            loggingConfiguration: LogConfiguration,
            developmentDebugConfig: DevelopmentDebugConfig,
            stepAggregatingLogReceiver: StepAggregatingLogReceiver,
            componentDependencies: SimulationComponentDependencies,
            executionContinuation: ExecutionContinuation,
        ): SimulationComponent {
            return DaggerSimulationComponent.factory()
                .create(
                    simulationParams,
                    loggingConfiguration,
                    developmentDebugConfig,
                    stepAggregatingLogReceiver,
                    executionContinuation,
                    componentDependencies,
                )
        }
    }
}

@Scope
annotation class SimulationScope