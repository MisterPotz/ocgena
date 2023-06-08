package simulation.client

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import simulation.config.SimulationConfig
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class Client(
    private val callback: OnReadinessCallback
) {
    private val ocDotStore: OcDotStore = OcDotStore()
    private val configStore = SimulationConfigStore()
    private val modelCreator = ModelCreator(
        ocDotStore,
        configStore
    )
    private val readinessDeducer = ModelSimulationReadinessDeducer(modelCreator)
    private val clientNotifier = ClientNotifier(callback, modelCreator, readinessDeducer)
    private val scope = MyCoroutineScope()
    private val currentErrors = MutableStateFlow<List<Error>?>(null)

    fun updateOcDot(ocDot: String) {
        println("Client.kt: updateOcDot")
        ocDotStore.updateOcDot(ocDot)
    }

    fun updateConfig(simulationConfigStore: SimulationConfig) {
        println("Client.kt: updateConfig")
        configStore.updatePlainConfig(simulationConfigStore)
    }

    fun createClientSimTaskFactory() : ClientSimTaskFactory? {
        val ocNet = modelCreator.builtModelFlow.value?.ocNet
        val simConfig = configStore.simulationConfigFlow.value

        if (ocNet == null || simConfig == null) {
            return null
        }

        return ClientSimTaskFactoryImpl(
            staticCoreOcNet = ocNet,
            config = simConfig,
        )
    }

    fun start() {
        scope.launch {
            launch {
                modelCreator.startModelCreation()
            }
            launch {
                readinessDeducer.startObserving()
            }
            launch {
                clientNotifier.startNotification()
            }
        }
    }
}
