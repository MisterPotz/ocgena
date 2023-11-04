package ru.misterpotz.ocgena.simulation.logging

import net.mamoe.yamlkt.Yaml
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.logging.loggers.*
import ru.misterpotz.ocgena.utils.ExecutedBindingDebugPrinter
import ru.misterpotz.ocgena.utils.TransitionInstanceDebugPrinter
import simulation.Logger
import simulation.client.loggers.CompoundLogger
import javax.inject.Inject
import javax.inject.Provider

class FullLoggerFactory @Inject constructor(
    private val logConfiguration: LogConfiguration,
    private val developmentDebugConfig: DevelopmentDebugConfig,
    private val simulationConfig: SimulationConfig,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val developmentWriterProvider: Provider<DevelopmentLogWriter>,
    private val stepAggregatingLogReceiver: StepAggregatingLogReceiver,
    private val stepAggregatingLogCreator: StepAggregatingLogCreator,
    private val printingUtility: TransitionInstanceDebugPrinter,
    private val executedBindingDebugPrinter: ExecutedBindingDebugPrinter,
    private val yaml: Yaml
) {
    fun createLogger(): Logger {
        val loggers = buildList {
            if (developmentDebugConfig.developmentLoggersEnabled) {
                add(
                    ANSIDebugTracingLogger(
                        currentSimulationDelegate = currentSimulationDelegate,
                        yaml = yaml,
                        developmentDebugConfig = developmentDebugConfig,
                        writer = developmentWriterProvider.get(),
                        simulationConfig = simulationConfig,
                        transitionInstanceDebugPrinter = printingUtility,
                        executedBindingDebugPrinter = executedBindingDebugPrinter
                    )
                )
            }
            if (logConfiguration.loggingEnabled) {
                add(
                    StepAggregatingLogger(
                        logReceiver = stepAggregatingLogReceiver,
                        stepAggregatingLogCreator = stepAggregatingLogCreator
                    )
                )
            }
        }
        return CompoundLogger(loggers)
    }
}
