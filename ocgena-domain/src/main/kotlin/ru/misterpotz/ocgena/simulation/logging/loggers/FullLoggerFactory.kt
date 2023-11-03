package ru.misterpotz.ocgena.simulation.logging.loggers

import net.mamoe.yamlkt.Yaml
import ru.misterpotz.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.simulation.logging.LogConfiguration
import simulation.Logger
import simulation.client.Writer
import simulation.client.loggers.CompoundLogger
import javax.inject.Inject
import javax.inject.Provider

class DevelopmentLogWriter @Inject constructor() : Writer {
    override fun writeLine(line: String) {
        println(line)
    }

    override fun end() {
        println()
    }
}

class FullLoggerFactory @Inject constructor(
    private val logConfiguration: LogConfiguration,
    private val developmentDebugConfig: DevelopmentDebugConfig,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val developmentWriterProvider: Provider<DevelopmentLogWriter>,
    private val stepAggregatingLogReceiver: StepAggregatingLogReceiver,
    private val stepAggregatingLogCreator: StepAggregatingLogCreator,
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
                        writer = developmentWriterProvider.get()
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
