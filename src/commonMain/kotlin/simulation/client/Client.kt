package simulation.client

import kotlinx.coroutines.*
import simulation.config.SimulationConfig
import kotlin.js.JsExport

@JsExport
class Client(
    private val onReadinessCallback: OnReadinessCallback
) {
    private val ocDotStore: OcDotStore = OcDotStore()
    private val configStore = SimulationConfigStore()
    private val modelCreator = ModelCreator(
        ocDotStore,
        configStore
    )
    private val readinessReporter = ModelSimulationReadinessReporter(modelCreator)
    private val notifier = Notifier(onReadinessCallback, readinessReporter)
    private val scope = MyCoroutineScope()

    fun updateOcDot(ocDot: String) {
        ocDotStore.updateOcDot(ocDot)
    }

    fun updateConfig(simulationConfigStore: SimulationConfig) {
        configStore.updatePlainConfig(simulationConfigStore)
    }

    fun createClientSimTask() : ClientSimTask? {
        val ocNet = modelCreator.builtModelFlow.value?.ocNet
        val simConfig = configStore.simulationConfigFlow.value

        if (ocNet == null || simConfig == null) {
            return null
        }

        return ClientSimTask(
            staticCoreOcNet = ocNet,
            config = simConfig,
            loggerWrapper = HtmlPrintingLoggerWrapper()
        )
    }

    fun start() {
        scope.launch {
            launch {
                modelCreator.startObserving()
            }
            launch {
                readinessReporter.startObserving()
            }
            launch {
                notifier.startObserving()
            }
        }
    }
}
