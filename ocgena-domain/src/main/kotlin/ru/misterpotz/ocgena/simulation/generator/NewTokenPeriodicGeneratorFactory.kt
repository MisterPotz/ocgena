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
    private val newTokenGenerationFacade: NewTokenGenerationFacade
) : NewTokenTimeBasedGeneratorFactory {
    override fun createGenerationQueue(simulationConfig: SimulationConfig): NewTokenTimeBasedGenerator {
        val ocNet = simulationConfig.ocNet

        return simulationConfig.tokenGeneration?.let {
            NormalNewTokenTimeBasedGenerator(
                tokenGenerationConfig = it,
                nextTimeSelector = newTokensGenerationTimeGenerator,
                placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry,
                newTokenGenerationFacade = newTokenGenerationFacade
            )
        } ?: NoOpNewTokenTimeBasedGenerator()
    }
}