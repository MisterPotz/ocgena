package simulation.client

import kotlinx.coroutines.Job
import model.*
import ru.misterpotz.simulation.config.SimulationConfig
import simulation.*
import simulation.client.loggers.*
import kotlin.js.ExperimentalJsExport
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

class LoggerWriters(
    val ansiWriter: Writer?,
    val ocelWriter : OcelWriter?,
    val additionalLoggers : List<Logger>
)

class DefaultSimTaskLoggerWrapper(
    private val loggingEnabled: Boolean,
    private val simTaskClientCallback: SimTaskClientCallback,
    private val htmlTraceFileWriter: Writer?,
    private val ansiTraceWriter : Writer?,
    private val ocelWriter: OcelWriter?,
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

        val ocelEventLogger = ocelWriter?.let {
            OcelEventLogger(
                ocelParams = OcelParams(logBothStartAndEnd = false),
                loggingEnabled = false,
                labelMapping = labelMapping,
                ocelWriter = it
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

@OptIn(ExperimentalJsExport::class)
@JsExport
interface ClientSimTaskFactory {
    fun create(
        onSimulationStatusUpdate : (ClientSimTaskStatus) -> Unit,
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
        onSimulationStatusUpdate : (ClientSimTaskStatus) -> Unit,
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

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ClientSimTaskStatus {
    JUST_CREATED,
    EXECUTING,
    FINISHED
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface ClientSimTask {
    val status : ClientSimTaskStatus

    fun finish()
    fun performStep(): Boolean

    fun prepareAndRun()

    fun finished() : Boolean {
        return status == ClientSimTaskStatus.FINISHED
    }

    fun isExecuting() = status == ClientSimTaskStatus.EXECUTING

    fun isJustCreated() = status == ClientSimTaskStatus.JUST_CREATED

}

class ClientSimTaskImpl(
    private val onSimulationStatusUpdate : (ClientSimTaskStatus) -> Unit,
    private val staticCoreOcNet: StaticCoreOcNet,
    private val config: SimulationConfig,
    private val loggerWrapper: SimTaskLoggerWrapper,
    private val dumpState : Boolean,
    ) : ClientSimTask {
    private var _status : ClientSimTaskStatus = ClientSimTaskStatus.JUST_CREATED
    init {
        notifyStatus()
    }
    override val status: ClientSimTaskStatus
        get() = _status

    override fun finish() {
        task.finish()
    }

    private fun notifyStatus() {
        onSimulationStatusUpdate(status)
    }

    private val simulationCreator = SimulationCreator(
        logger = object : LoggerFactory {
            override fun create(labelMapping: LabelMapping): Logger {
                return CompoundLogger(
                    loggingEnabled = true,
                    loggers = arrayOf(
                        object : NoOpLogger() {
                            override fun onEnd() {
                                _status = ClientSimTaskStatus.FINISHED
                                notifyStatus()
                            }
                        },
                        loggerWrapper.createLogger(labelMapping)
                    )
                )
            }
        }
    )
    private val task = simulationCreator.createSimulationTask()
    private val myCoroutineScope = MyCoroutineScope()

    private var jobba: Job? = null


    override fun prepareAndRun() {
        while(!performStep()) { }
    }

    override fun performStep(): Boolean {
        return when (_status) {
            ClientSimTaskStatus.JUST_CREATED -> {
                _status = ClientSimTaskStatus.EXECUTING
                notifyStatus()
                task.prepareRun()
                false
            }

            ClientSimTaskStatus.EXECUTING -> {
                task.doRunStep()
                false;
            }

            ClientSimTaskStatus.FINISHED -> {
                true
            }
        }
    }
}
