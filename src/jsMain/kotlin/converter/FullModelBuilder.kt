package converter

import config.SimulationConfig

class FullModelBuilder {
    private val simulationConfigStore = SimulationConfigStore()
    private var ocDot: String? = null

    fun accept(simulationConfig: SimulationConfig) {
        simulationConfigStore.updateSimulationConfig(simulationConfig)
    }

    fun accept(ocdot: String) {
        this.ocDot = ocdot
    }

    fun process(): OcDotParseResult? {
        val ocdot = ocDot ?: return null
        val domainConfig = {
            simulationConfigStore.requireProcessingResult()
        }.takeIf { !simulationConfigStore.isEmpty() }?.invoke() ?: return null

        val ocDotParseProcessor = OcDotParseProcessor(domainConfig.placeTyping, domainConfig.inputOutputPlaces)
        val processingResult = ocDotParseProcessor.process(ocdot)

        return processingResult
    }
}
