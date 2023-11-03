package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.generator.impl.NoOpNewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation.generator.impl.NormalNewTokenTimeBasedGenerator
import javax.inject.Inject

interface NewTokenTimeBasedGeneratorFactory {
    fun createGenerationQueue(simulationConfig: SimulationConfig): NewTokenTimeBasedGenerator
}

class NewTokenTimeBasedGeneratorFactoryImpl @Inject constructor(
    private val newTokensGenerationTimeGenerator: NewTokensGenerationTimeGenerator,
    private val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade
) : NewTokenTimeBasedGeneratorFactory {
    override fun createGenerationQueue(simulationConfig: SimulationConfig): NewTokenTimeBasedGenerator {
        val ocNet = simulationConfig.templateOcNet

        return simulationConfig.generationConfig?.let {
            NormalNewTokenTimeBasedGenerator(
                generationConfig = it,
                nextTimeSelector = newTokensGenerationTimeGenerator,
                placeToObjectTypeRegistry = ocNet.coreOcNet.placeToObjectTypeRegistry,
                newTokenTimeBasedGenerationFacade = newTokenTimeBasedGenerationFacade
            )
        } ?: NoOpNewTokenTimeBasedGenerator()
    }
}