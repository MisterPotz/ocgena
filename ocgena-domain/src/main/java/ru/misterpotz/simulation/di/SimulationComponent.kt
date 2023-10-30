package ru.misterpotz.simulation.di

import dagger.*
import kotlinx.serialization.json.Json
import model.LabelMapping
import model.OcNetType
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.simulation.logging.LogConfiguration
import ru.misterpotz.simulation.transition.TransitionDurationSelector
import ru.misterpotz.simulation.transition.TransitionInstanceOccurenceDeltaSelector
import simulation.*
import simulation.binding.*
import simulation.client.LoggerWriters
import simulation.client.OcelParams
import simulation.client.SimTaskLoggerWrapper
import simulation.client.loggers.*
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
    abstract fun bindingSelector(bindingSelector: BindingSelectorImpl): BindingSelector

    @Provides
    @SimulationScope
    fun executionConditions(): ExecutionConditions {
        return SimpleExecutionConditions()
    }

    @Provides
    @SimulationScope
    fun logger(loggerFactory: LoggerFactory, labelMapping: LabelMapping): Logger {
        return loggerFactory.create(labelMapping)
    }

    @Provides
    @SimulationScope
    fun random(randomFactory: RandomFactory): Random {
        return randomFactory.create()
    }

    @Provides
    @SimulationScope
    fun labelMapping(simulationConfig: SimulationConfig): LabelMapping {
        return simulationConfig.labelMapping
    }

    @Binds
    @SimulationScope
    abstract fun simulationStateProvider(simulationStateProviderImpl: SimulationStateProviderImpl):
            SimulationStateProvider

    @Provides
    @SimulationScope
    fun provideTransitionDurationSelector(
        random: Random,
        simulationConfig: SimulationConfig
    ): TransitionDurationSelector {
        return TransitionDurationSelector(
            random,
            intervalFunction = simulationConfig.templateOcNet.intervalFunction
        )
    }

    @Provides
    @SimulationScope
    fun transitionInstanceOccurrenceDeltaSelector(
        random: Random,
        simulationConfig: SimulationConfig
    ): TransitionInstanceOccurenceDeltaSelector {
        return TransitionInstanceOccurenceDeltaSelector(
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
    fun bindingOutputMarkingResolver(bindingOutputMarkingResolverFactory: BindingOutputMarkingResolverFactory): InputToOutputPlaceResolver {
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
    abstract fun simulationTaskStepExecutor(simulationTaskStepExecutor: SimulationTaskStepExecutor):
            SimulationTaskStepExecutor

    @Binds
    @SimulationScope
    abstract fun simTaskStepExecutorFactory(factory: SimulationTaskStepExecutorFactoryImpl):
            SimulationTaskStepExecutorFactory

    @Binds
    @SimulationScope
    abstract fun activeTransitionFinisher(activeTransitionFinisherImpl: ActiveTransitionFinisherImpl):
            ActiveTransitionMarkingFinisher

    @Provides
    @SimulationScope
    fun provideLoggerFactory(
        developmentDebugConfig: DevelopmentDebugConfig,
        loggingWriters: LoggerWriters,
        loggingConfiguration: LogConfiguration,
        simulationConfig: SimulationConfig,
    ): LoggerFactory {
        val labelMapping = simulationConfig.labelMapping

        return CompoundLogger(
            loggingEnabled = true,
            loggers = buildList {
                SimTaskLoggerWrapper()
                val htmlDebugTraceLogger = htmlTraceFileWriter?.let {
                    HtmlExecutionPrintingLogger(
                        loggingEnabled = loggingEnabled,
                        labelMapping = labelMapping,
                        writer = it,
                    )
                }

                val ansiTraceLogger = ansiTraceWriter?.let {
                    ANSITracingLogger(
                        loggingEnabled,
                        writer = it
                    )
                }

                val ocelEventLogger = ocelWriter?.let {
                    OcelEventLogger(
                        ocelParams = OcelParams(logBothStartAndEnd = false),
                        loggingEnabled = false,
                        labelMapping = labelMapping,
                        ocelWriter = it
                    )
                }
                addAll(loggingWriters.additionalLoggers)
            }
        )
    }
}

interface SimulationComponentDependencies {
    val json: Json
}


@SimulationScope
@Component(
    modules = [SimulationModule::class],
    dependencies = [SimulationComponentDependencies::class]
)
interface SimulationComponent {
    fun simulationTaskExecutor(): SimulationTaskStepExecutorFactory
    fun simulationTask(): SimulationTask

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance simulationParams: SimulationConfig,
            @BindsInstance loggerFactory: LoggerFactory,
            @BindsInstance randomFactory: RandomFactory,
            @BindsInstance developmentDebugConfig: DevelopmentDebugConfig,
        ): SimulationComponent
    }
}

@Scope
annotation class SimulationScope