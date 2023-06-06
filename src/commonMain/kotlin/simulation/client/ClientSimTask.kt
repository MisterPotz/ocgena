package simulation.client

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import model.*
import simulation.*
import simulation.client.loggers.CompoundLogger
import simulation.client.loggers.HtmlExecutionPrintingLogger
import simulation.client.loggers.OcelEventLogger
import simulation.utils.createParams
import kotlin.js.JsExport

@JsExport
interface SimTaskClientCallback {
    fun onExecutionFinish()
    fun onExecutionStart()
    fun onExecutionTimeout()
}

@JsExport
fun createSimpleClientCallback(
    onExecutionFinish: () -> Unit = { },
    onExecutionStart : () -> Unit = { },
    onExecutionTimeout: () -> Unit = { }
) : SimTaskClientCallback {
    return object : SimTaskClientCallback {
        override fun onExecutionFinish() {
            onExecutionFinish()
        }

        override fun onExecutionStart() {
            onExecutionStart()
        }

        override fun onExecutionTimeout() {
            onExecutionTimeout()
        }
    }
}

interface SimTaskLoggerWrapper {
    fun createLogger(labelMapping: LabelMapping): Logger
}

class DefaultSimTaskLoggerWrapper(
    private val loggingEnabled: Boolean,
    private val simTaskClientCallback: SimTaskClientCallback,
    private val htmlTraceFileWriter: Writer,
    private val ocelWriter: OcelWriter,
) : SimTaskLoggerWrapper {
    override fun createLogger(labelMapping: LabelMapping): Logger {
        val htmlDebugTraceLogger = HtmlExecutionPrintingLogger(
            loggingEnabled = loggingEnabled,
            labelMapping = labelMapping,
            writer = htmlTraceFileWriter,
        )

        val ocelEventLogger = OcelEventLogger(
            ocelParams = OcelParams(logBothStartAndEnd = false),
            loggingEnabled = false,
            labelMapping = labelMapping,
            ocelWriter = ocelWriter
        )
        val callbackLogger = CallbackLogger(simTaskClientCallback = simTaskClientCallback)

        return CompoundLogger(
            loggingEnabled = loggingEnabled,
            loggers = arrayOf(
                htmlDebugTraceLogger,
                ocelEventLogger,
                callbackLogger
            )
        )
    }
}

@JsExport
interface ClientSimTaskFactory {
    fun create(
        simTaskClientCallback: SimTaskClientCallback,
        htmlTraceFileWriter: Writer,
        ocelWriter: OcelWriter,
    ): ClientSimTask
}

class ClientSimTaskFactoryImpl (
    private val staticCoreOcNet: StaticCoreOcNet,
    private val config: ProcessedSimulationConfig,
) : ClientSimTaskFactory {
    override fun create(
        simTaskClientCallback: SimTaskClientCallback,
        htmlTraceFileWriter: Writer,
        ocelWriter: OcelWriter,
    ): ClientSimTask {
        return ClientSimTaskImpl(
            loggerWrapper = DefaultSimTaskLoggerWrapper(
                loggingEnabled = true,
                simTaskClientCallback = simTaskClientCallback,
                htmlTraceFileWriter = htmlTraceFileWriter,
                ocelWriter = ocelWriter,
            ),
            staticCoreOcNet = staticCoreOcNet,
            config = config
        )
    }
}

@JsExport
interface ClientSimTask {
    fun launch()
}

class ClientSimTaskImpl(
    private val staticCoreOcNet: StaticCoreOcNet,
    private val config: ProcessedSimulationConfig,
    private val loggerWrapper: SimTaskLoggerWrapper,

    ) : ClientSimTask {
    private val simulationCreator = SimulationCreator(
        simulationParams = createParams(staticCoreOcNet, config),
        executionConditions = SimpleExecutionConditions(),
        logger = object : LoggerFactory {
            override fun create(labelMapping: LabelMapping): Logger {
                return loggerWrapper.createLogger(labelMapping)
            }
        }
    )
    private val task = simulationCreator.createSimulationTask()
    private val myCoroutineScope = MyCoroutineScope()

    private var jobba: Job? = null

    override fun launch() {
        if (jobba?.isActive == true) return

        jobba = myCoroutineScope.launch {
            task.prepareAndRun()
        }
    }
}
