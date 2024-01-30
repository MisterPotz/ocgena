package simulation.random

import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.di.RandomInstanceInput
import javax.inject.Inject
import kotlin.random.Random

class RandomFactoryImpl @Inject constructor(
    simulationConfig: SimulationConfig,
    @RandomInstanceInput
    private val randomInputInstance: Random?
) : RandomFactory {
    private val randomSeed: Int? = simulationConfig.randomSeed

    override fun create(): Random {
        return randomInputInstance
            ?: randomSeed?.let { Random(randomSeed) }
            ?: Random
    }
}