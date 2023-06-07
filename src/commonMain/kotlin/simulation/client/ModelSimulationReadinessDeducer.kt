package simulation.client

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import model.OcDotParseResult

class ModelSimulationReadinessDeducer(
    private val modelCreator: ModelCreator
) {
    data class Readiness(
        val canStartSimulation: Boolean,
        val ocDotParseResult: OcDotParseResult?,
    )

    val readinessFlow: MutableStateFlow<Readiness> = MutableStateFlow(
        Readiness(false, null)
    )

    suspend fun startObserving() {
        modelCreator.builtModelFlow.collectLatest {
            if (it != null) {
                val hasCriticalErrors = it.hasCriticalErrors
                readinessFlow.emit(
                    Readiness(
                        canStartSimulation = !hasCriticalErrors,
                        ocDotParseResult = it
                    )
                )
            } else {
                readinessFlow.emit(Readiness(false, null))
            }
        }
    }
}
