package ru.misterpotz.ocgena.simulation_old.di

import com.charleskorn.kaml.Yaml
import dagger.*
import kotlinx.serialization.json.Json
import ru.misterpotz.Logger
import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenRealAmountRegistryImpl
import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenSet
import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenSetMap
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
import ru.misterpotz.ocgena.simulation_old.*
import ru.misterpotz.ocgena.simulation_old.binding.TIFinisher
import ru.misterpotz.ocgena.simulation_old.binding.TIFinisherImpl
import ru.misterpotz.ocgena.simulation_old.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation_old.binding.consumer.OutputTokensBufferConsumerFactory
import ru.misterpotz.ocgena.simulation_old.binding.generator.OutputMissingTokensGeneratorFactory
import ru.misterpotz.ocgena.simulation_old.binding.groupstrat.ByObjTypeAndArcGroupingStrategy
import ru.misterpotz.ocgena.simulation_old.binding.groupstrat.ByObjTypeGroupingStrategy
import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.simulation_old.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation_old.continuation.ExecutionContinuation
import ru.misterpotz.ocgena.simulation_old.continuation.NoOpExecutionContinuation
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenGenerationFacade
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenTimeBasedGeneratorFactory
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenTimeBasedGeneratorFactoryImpl
import ru.misterpotz.ocgena.simulation_old.generator.original.TransitionInstanceDurationGeneratorOriginal
import ru.misterpotz.ocgena.simulation_old.generator.original.TransitionNextInstanceAllowedTimeGeneratorOriginal
import ru.misterpotz.ocgena.simulation_old.interactors.*
import ru.misterpotz.ocgena.simulation_old.interactors.original.EnabledBindingResolverInteractorOriginalImpl
import ru.misterpotz.ocgena.simulation_old.logging.*
import ru.misterpotz.ocgena.simulation_old.logging.loggers.NoOpStepAggregatingLogReceiver
import ru.misterpotz.ocgena.simulation_old.logging.loggers.StepAggregatingLogReceiver
import ru.misterpotz.ocgena.simulation_old.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation_old.state.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation_old.state.CurrentSimulationDelegateImpl
import ru.misterpotz.ocgena.simulation_old.state.PMarkingProvider
import ru.misterpotz.ocgena.simulation_old.state.StateImpl
import ru.misterpotz.ocgena.simulation_old.state.original.CurrentSimulationStateOriginal
import ru.misterpotz.ocgena.simulation_old.stepexecutor.*
import ru.misterpotz.ocgena.simulation_old.stepexecutor.timepn.NewTimeDeltaInteractor
import ru.misterpotz.ocgena.simulation_old.structure.SimulatableOcNetInstance
import ru.misterpotz.ocgena.simulation_old.structure.SimulatableOcNetInstanceImpl
import ru.misterpotz.ocgena.simulation_old.structure.State
import ru.misterpotz.ocgena.simulation_old.token_generation.ObjectTokenGenerator
import ru.misterpotz.ocgena.simulation_old.transition.TransitionInstanceCreatorFacadeOriginal
import simulation.binding.BindingOutputMarkingResolverFactory
import simulation.binding.BindingOutputMarkingResolverFactoryImpl
import simulation.random.RandomFactoryImpl
import simulation.random.RandomSource
import simulation.random.RandomUseCase
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

    @Binds
    @SimulationScope
    abstract fun bindSimulationStepLogger(simulationStepLoggerImpl: SimulationStepLoggerImpl): SimulationStepLogger

    companion object {
        @Provides
        @SimulationScope
        fun logger(fullLoggerFactory: FullLoggerFactory): SimulationDBLogger {
            return fullLoggerFactory.createLogger()
        }

        @Provides
        @SimulationScope
        fun provideTimePNTransitionMarking(ocNet: OCNet): TimePNTransitionMarking {
            return TimePNTransitionMarking(ocNet.transitionsRegistry.iterable.map { it.id })
        }

        @Provides
        @SimulationScope
        fun random(
            randomFactory: RandomFactoryImpl,
        ): RandomSource {
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
            random: RandomSource,
            simulationConfig: SimulationConfig,
        ): TransitionInstanceDurationGeneratorOriginal {
            return TransitionInstanceDurationGeneratorOriginal(
                random.backwardSupport(),
                transitionsOriginalSpec = simulationConfig.castTransitions()
            )
        }

        @Provides
        @SimulationScope
        fun providesObjectRealTokenAmountRegistry(ocNet: OCNet): ObjectTokenRealAmountRegistry {
            return ObjectTokenRealAmountRegistryImpl().apply {
                for (place in ocNet.placeRegistry.places) {
                    placeToTokens[place.id] = 0
                }
            }
        }

        @Provides
        @SimulationScope
        fun providePlaceToObjectTypeRegistry(ocNet: OCNet): PlaceToObjectTypeRegistry {
            return ocNet.placeToObjectTypeRegistry
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
            timePNPreparatorProvider: Provider<SimulationTaskTimePNPreparator>,
        ): SimulationTaskPreparator {
            val simulationTaskPreparator = when (simulationConfig.simulationSemantics.type) {
                SimulationSemanticsType.ORIGINAL -> {
                    require(simulationConfig.transitionsSpec is TransitionsOriginalSpec)
                    val new = simulationTaskPreparatorProvider.get()
                    require(new is SimulationTaskPreparatorOriginal)
                    new
                }

                SimulationSemanticsType.SIMPLE_TIME_PN -> {
                    timePNPreparatorProvider.get() as SimulationTaskTimePNPreparator
                }
            }
            return simulationTaskPreparator
        }

        @Provides
        @SimulationScope
        fun provideTransitionNextInstanceAllowedTimeGenerator(
            random: RandomSource,
            simulationConfig: SimulationConfig,
        ): TransitionNextInstanceAllowedTimeGeneratorOriginal {
            return TransitionNextInstanceAllowedTimeGeneratorOriginal(
                random = random.backwardSupport(),
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
        fun transitionsRegistry(ocNet: OCNet): TransitionsRegistry {
            return ocNet.transitionsRegistry
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
            simulationDBLogger: SimulationDBLogger,
            newTokenTimeBasedGenerator: NewTokenTimeBasedGenerator,
            enabledBindingsCollectorInteractor: EnabledBindingsCollectorInteractor,
            objectTokenSet: ObjectTokenSet,
            objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
            currentSimulationStateOriginal: Provider<CurrentSimulationStateOriginal>,
            timePNStepExecutor: Provider<TimePetriNetStepExecutor>,
        ): StepExecutor {
            return when (simulationConfig.simulationSemantics.type) {
                SimulationSemanticsType.ORIGINAL -> {
                    OriginalStepExecutor(
                        simulationStateProvider,
                        bindingSelectionInteractor = bindingSelectionInteractor,
                        transitionFinisher = tiFinisher,
                        transitionInstanceCreatorFacadeOriginal = transitionToInstancesRegistryOriginal.get(),
                        simulationDBLogger = simulationDBLogger,
                        newTokenTimeBasedGenerator = newTokenTimeBasedGenerator,
                        bindingsCollector = enabledBindingsCollectorInteractor,
                        objectTokenSet = objectTokenSet,
                        objectTokenRealAmountRegistry = objectTokenRealAmountRegistry,
                        currentSimulationStateOriginal = currentSimulationStateOriginal.get()
                    )
                }

                SimulationSemanticsType.SIMPLE_TIME_PN -> {
                    timePNStepExecutor.get()
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
        ): TokenGroupedInfo.TokenGroupingStrategy {
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
    fun dbLogger(): Logger
}

@SimulationScope
@Component(
    modules = [SimulationModule::class],
    dependencies = [SimulationComponentDependencies::class]
)
interface SimulationComponent {
    fun simulationTask(): SimulationTask
    fun simulationTaskPreparator(): SimulationTaskPreparator
    fun stepExecutor(): StepExecutor
    fun simulationConfig(): SimulationConfig
    fun state(): State
    fun ocNet(): OCNet
    fun enabledBindingsResolver(): EnabledBindingResolverInteractor
    fun objectTokenRealAmountRegistry(): ObjectTokenRealAmountRegistry
    fun simulationStateProvider(): SimulationStateProvider

    @GlobalTokenBunch
    fun tokenBunch(): SparseTokenBunch
    fun transitionFiringRuleExecutor(): TransitionFiringRuleExecutor
    fun outputTokensBufferConsumerFactory(): OutputTokensBufferConsumerFactory
    fun outputMissingTokensGeneratorFactory(): OutputMissingTokensGeneratorFactory
    fun batchGroupingStrategy(): TokenGroupedInfo.TokenGroupingStrategy
    fun objectTokenSet(): ObjectTokenSet
    fun objectTokenGenerator(): ObjectTokenGenerator
    fun newTokenGenerationFacade(): NewTokenGenerationFacade
    fun tokenSelectionInteractor(): TokenSelectionInteractor
    fun prePostPlaceRegistry(): PrePlaceRegistry
    fun arcPrePlaceHasEnoughTokensChecker(): ArcPrePlaceHasEnoughTokensChecker

    fun newTimeDeltaInteractor(): NewTimeDeltaInteractor

    fun timePNTransitionMarking(): TimePNTransitionMarking

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance simulationParams: SimulationConfig,
            @BindsInstance loggingConfiguration: LogConfiguration,
            @BindsInstance developmentDebugConfig: DevelopmentDebugConfig,
            @BindsInstance stepAggregatingLogReceiver: StepAggregatingLogReceiver,
            @BindsInstance
            executionContinuation: ExecutionContinuation,
            @BindsInstance
            randomInstances: Map<@JvmSuppressWildcards RandomUseCase, @JvmSuppressWildcards Random>,
            @BindsInstance
            defaultTimePNProvider: DefaultTimePNProvider,
            @BindsInstance
            determinedTransitionSequenceProvider: DeterminedTransitionSequenceProvider,
            componentDependencies: SimulationComponentDependencies,
        ): SimulationComponent
    }

    companion object {
        fun defaultCreate(
            simulationConfig: SimulationConfig,
            componentDependencies: SimulationComponentDependencies,
            determinedTransitionSequenceProvider: DeterminedTransitionSequenceProvider = SimpleDeterminedTransitionSequenceProvider(
                listOf()
            ),
            developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
            randomInstances: Map<@JvmSuppressWildcards RandomUseCase, @JvmSuppressWildcards Random> = mutableMapOf(),
            executionContinuation: ExecutionContinuation = NoOpExecutionContinuation(),
        ): SimulationComponent {
            return create(
                simulationParams = simulationConfig,
                loggingConfiguration = LogConfiguration.default(),
                developmentDebugConfig = developmentDebugConfig,
                stepAggregatingLogReceiver = NoOpStepAggregatingLogReceiver,
                componentDependencies = componentDependencies,
                executionContinuation = executionContinuation,
                defaultTimePNProvider = DefaultTimePNProvider(),
                randomInstances = randomInstances,
                determinedTransitionSequenceProvider = determinedTransitionSequenceProvider
            )
        }

        fun create(
            simulationParams: SimulationConfig,
            loggingConfiguration: LogConfiguration,
            developmentDebugConfig: DevelopmentDebugConfig,
            stepAggregatingLogReceiver: StepAggregatingLogReceiver,
            componentDependencies: SimulationComponentDependencies,
            executionContinuation: ExecutionContinuation,
            defaultTimePNProvider: DefaultTimePNProvider,
            randomInstances: Map<@JvmSuppressWildcards RandomUseCase, @JvmSuppressWildcards Random>,
            determinedTransitionSequenceProvider: DeterminedTransitionSequenceProvider,
        ): SimulationComponent {
            return DaggerSimulationComponent.factory()
                .create(
                    simulationParams,
                    loggingConfiguration,
                    developmentDebugConfig,
                    stepAggregatingLogReceiver,
                    executionContinuation,
                    randomInstances,
                    defaultTimePNProvider,
                    determinedTransitionSequenceProvider,
                    componentDependencies,
                )
        }
    }
}

@Scope
annotation class SimulationScope
