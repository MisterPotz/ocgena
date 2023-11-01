package simulation.random

import ru.misterpotz.simulation.config.SimulationConfig
import javax.inject.Inject
import kotlin.random.Random

class RandomFactoryImpl @Inject constructor(
    simulationConfig: SimulationConfig,
) : RandomFactory {
    private val randomSeed: Int? = simulationConfig.randomSeed

    override fun create(): Random {
        return randomSeed?.let { Random(randomSeed) } ?: Random
    }
}