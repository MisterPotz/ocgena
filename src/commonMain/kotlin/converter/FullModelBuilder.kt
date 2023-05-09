package converter

import simulation.ProcessedSimulationConfig


class FullModelBuilder {
    private var simulationConfig : ProcessedSimulationConfig? = null
    private var ocDot: String? = null

    fun withConfig(simulationConfig: ProcessedSimulationConfig) {
        this.simulationConfig = simulationConfig
    }

    fun withOcDot(ocdot: String) {
        this.ocDot = ocdot
    }

    fun newTask() : FullModelBuildingTask {
        return FullModelBuildingTask(
            ocDot = ocDot!!,
            processedConfig = simulationConfig!!
        )
    }
}
