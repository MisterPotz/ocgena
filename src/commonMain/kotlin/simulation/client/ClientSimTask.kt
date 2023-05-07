package simulation.client

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import model.*
import simulation.*
import simulation.utils.createParams
import kotlin.js.JsExport


interface LoggerWrapper {
    fun createLogger(labelMapping: LabelMapping): Logger
}

@JsExport
class HtmlPrintingLoggerWrapper : LoggerWrapper {
    override fun createLogger(labelMapping: LabelMapping): Logger {
        return HtmlExecutionPrintingLogger(loggingEnabled = true, labelMapping)
    }

}

@JsExport
class ClientSimTask(
    private val loggerWrapper: LoggerWrapper,
    private val staticCoreOcNet: StaticCoreOcNet,
    private val config: ProcessedSimulationConfig
) {
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

    fun launch(simCallback: SimCallback) {
        if (jobba?.isActive == true) return

        jobba = myCoroutineScope.launch {
            task.prepareAndRun()
            simCallback.onFinishedSimulation()
        }
    }
}
