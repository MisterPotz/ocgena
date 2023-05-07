package simulation.client

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import utils.mprintln

class Notifier(
    private val callback: OnReadinessCallback,
    private val modelSimulationReadinessReporter: ModelSimulationReadinessReporter
) {
    @OptIn(FlowPreview::class)
    suspend fun startObserving() {
        modelSimulationReadinessReporter
            .readinessFlow
            .onEach {
                mprintln("current readiness $it")
            }
            .debounce(100L)
            .collectLatest {
                callback.readyToCalc(it.canStartSimulation)
            }
    }
}
