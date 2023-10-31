package simulation.client

import model.*
import simulation.*
import simulation.client.loggers.*

interface SimTaskClientCallback {
    fun onExecutionFinish()
    fun onExecutionStart()
    fun onExecutionTimeout()
}

fun createSimpleClientCallback(
    onExecutionFinish: () -> Unit = { },
    onExecutionStart: () -> Unit = { },
    onExecutionTimeout: () -> Unit = { }
): SimTaskClientCallback {
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
    private val htmlTraceFileWriter: Writer?,
    private val ansiTraceWriter: Writer?,
) : SimTaskLoggerWrapper {
    override fun createLogger(labelMapping: LabelMapping): Logger {
        val htmlDebugTraceLogger = htmlTraceFileWriter?.let {
            HtmlExecutionPrintingLogger(
                loggingEnabled = loggingEnabled,
                labelMapping = labelMapping,
                writer = it,
            )
        }

        val ansiTraceLogger = ansiTraceWriter?.let {
            ANSIDebugTracingLogger(
                loggingEnabled,
                writer = it
            )
        }
        val callbackLogger = CallbackLogger(simTaskClientCallback = simTaskClientCallback)

        return CompoundLogger(
            loggingEnabled = loggingEnabled,
            loggers = buildList {
                if (htmlDebugTraceLogger != null) {
                    add(htmlDebugTraceLogger)
                }
                if (ansiTraceLogger != null) {
                    add(ansiTraceLogger)
                }
                if (ocelEventLogger != null) {
                    add(ocelEventLogger)
                }
                add(callbackLogger)
            }.toTypedArray()
        )
    }
}

interface ClientSimTaskFactory {
    fun create(
        onSimulationStatusUpdate: (ClientSimTaskStatus) -> Unit,
        simTaskClientCallback: SimTaskClientCallback,
        htmlTraceFileWriter: Writer?,
        ansiTraceWriter: Writer?,
        ocelWriter: OcelWriter?,
    ): ClientSimTask
}

class ClientSimTaskFactoryImpl(
    private val staticCoreOcNet: StaticCoreOcNet,
    private val config: ProcessedSimulationConfig,
    private val loggingEnabled: Boolean,
    private val dumpState: Boolean = false
) : ClientSimTaskFactory {
    override fun create(
        onSimulationStatusUpdate: (ClientSimTaskStatus) -> Unit,
        simTaskClientCallback: SimTaskClientCallback,
        htmlTraceFileWriter: Writer?,
        ansiTraceWriter: Writer?,
        ocelWriter: OcelWriter?,
    ): ClientSimTask {
        return ClientSimTaskImpl(
            loggerWrapper = DefaultSimTaskLoggerWrapper(
                loggingEnabled = loggingEnabled,
                simTaskClientCallback = simTaskClientCallback,
                htmlTraceFileWriter = htmlTraceFileWriter,
                ansiTraceWriter = ansiTraceWriter,
                ocelWriter = ocelWriter,
            ),
            staticCoreOcNet = staticCoreOcNet,
            config = config,
            onSimulationStatusUpdate = onSimulationStatusUpdate,
            dumpState = dumpState
        )
    }
}

interface ClientSimTask {
    val status: ClientSimTaskStatus

    fun finish()
    fun performStep(): Boolean

    fun prepareAndRun()

    fun finished(): Boolean {
        return status == ClientSimTaskStatus.FINISHED
    }

    fun isExecuting() = status == ClientSimTaskStatus.EXECUTING

    fun isJustCreated() = status == ClientSimTaskStatus.JUST_CREATED

}

