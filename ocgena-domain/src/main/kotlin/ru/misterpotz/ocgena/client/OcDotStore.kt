package simulation.client

import kotlinx.coroutines.flow.MutableStateFlow

class OcDotStore() {
    val ocDotFlow: MutableStateFlow<String?> = MutableStateFlow(null)

    fun updateOcDot(ocDot: String) {
        ocDotFlow.value = ocDot
    }
}
