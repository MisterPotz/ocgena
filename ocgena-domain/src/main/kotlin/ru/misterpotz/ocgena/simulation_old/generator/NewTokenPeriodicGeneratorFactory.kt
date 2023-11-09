package ru.misterpotz.ocgena.simulation_old.generator

import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.simulation_old.generator.impl.NoOpNewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation_old.generator.impl.NormalNewTokenTimeBasedGenerator
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