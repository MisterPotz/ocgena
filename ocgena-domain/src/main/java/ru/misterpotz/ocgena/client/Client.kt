package simulation.client

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import simulation.config.ConfigHolder
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
    var loggingEnabled = true
    var dumpState = false

    fun updateOcDot(ocDot: String) {
        println("Client.kt: updateOcDot")
        ocDotStore.updateOcDot(ocDot)
    }

    fun updateConfig(configHolderStore: ConfigHolder) {
        println("Client.kt: updateConfig")
        configStore.updatePlainConfig(configHolderStore)
    }

    fun createFactoryFromData(
        ocDot: String,
        configHolder: ConfigHolder,
    ): ClientSimTaskFactory? {
        val processedSimulationConfig = configStore.mapSimulationConfigSafely(configHolder) ?: return null
        val model = modelCreator.createModelSafely(ocDot, processedSimulationConfig) ?: return null

        return ClientSimTaskFactoryImpl(
            staticCoreOcNet = model.ocNet ?: return null,
            config = processedSimulationConfig,
            loggingEnabled = loggingEnabled,
            dumpState = dumpState
        )
    }

//    fun createClientSimTaskFactory(): ClientSimTaskFactory? {
//        val ocNet = modelCreator.builtModelFlow.value?.ocNet
//        val simConfig = configStore.simulationConfigFlow.value
//
//        if (ocNet == null || simConfig == null) {
//            return null
//        }
//
//        return ClientSimTaskFactoryImpl(
//            staticCoreOcNet = ocNet,
//            config = simConfig,
//            loggingEnabled = loggingEnabled
//        )
//    }

    fun createClientSimTaskFactory(): ClientSimTaskFactory? {
        // take latest values
        val processedSimulationConfig = configStore.simulationConfigFlow.value ?: return null
        val model = modelCreator.createModelSafely(ocDotStore.ocDotFlow.value ?: return null, processedSimulationConfig)
            ?: return null

        return ClientSimTaskFactoryImpl(
            staticCoreOcNet = model.ocNet ?: return null,
            config = processedSimulationConfig,
            loggingEnabled = loggingEnabled,
            dumpState = dumpState
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
