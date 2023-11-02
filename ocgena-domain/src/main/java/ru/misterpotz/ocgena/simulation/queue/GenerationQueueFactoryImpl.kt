package ru.misterpotz.ocgena.simulation.queue

import ru.misterpotz.simulation.config.SimulationConfig
import simulation.TokenGenerationTimeSelector
import javax.inject.Inject

class GenerationQueueFactoryImpl @Inject constructor(
    private val tokenGenerationTimeSelector: TokenGenerationTimeSelector,
    private val tokenGenerationFacade: TokenGenerationFacade
) : GenerationQueueFactory {
    override fun createGenerationQueue(simulationConfig: SimulationConfig): GenerationQueue {
        val ocNet = simulationConfig.templateOcNet

        return simulationConfig.generationConfig?.let {
            NormalGenerationQueue(
                generationConfig = it,
                nextTimeSelector = tokenGenerationTimeSelector,
                placeObjectTypeRegistry = ocNet.coreOcNet.placeTyping,
                tokenGenerationFacade = tokenGenerationFacade
            )
        } ?: NoOpGenerationQueue()
    }
}