package ru.misterpotz.ocgena.simulation_old.logging

import com.charleskorn.kaml.Yaml
import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.simulation_old.logging.loggers.*
import ru.misterpotz.ocgena.simulation_old.state.CurrentSimulationDelegate
import ru.misterpotz.ocgena.utils.ExecutedBindingDebugPrinter
import ru.misterpotz.ocgena.utils.TransitionInstanceDebugPrinter
import ru.misterpotz.ocgena.simulation_old.logging.loggers.CompoundSimulationDBLogger
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
    fun createLogger(): SimulationDBLogger {
        val loggers = buildList {
            if (developmentDebugConfig.developmentLoggersEnabled) {
                add(
                    ANSIDebugTracingSimulationDBLogger(
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
                    StepAggregatingSimulationDBLogger(
                        logReceiver = stepAggregatingLogReceiver,
                        stepAggregatingLogCreator = stepAggregatingLogCreator
                    )
                )
            }
        }
        return CompoundSimulationDBLogger(loggers)
    }
}
