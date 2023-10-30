package simulation.client

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import utils.mprintln

class ClientNotifier(
    private val callback: OnReadinessCallback,
    private val modelCreator: ModelCreator,
    private val modelSimulationReadinessDeducer: ModelSimulationReadinessDeducer
) {
    @OptIn(FlowPreview::class)
    suspend fun startNotification() {
        coroutineScope {
            launch {
                modelSimulationReadinessDeducer
                    .readinessFlow
                    .onEach {
                        mprintln("current readiness $it")
                    }
                    .debounce(100L)
                    .collectLatest {
                        callback.readyToCalc(it.canStartSimulation)
                        if (it.ocDotParseResult != null) {
                            callback.ocDotParseResult(it.ocDotParseResult)
                        }
                    }
            }
            launch {
                modelCreator.errorsFlow.debounce(200L)
                    .collectLatest {
                        callback.onCurrentErrorsChange(it?.toTypedArray())
                    }
            }
        }
    }
}
