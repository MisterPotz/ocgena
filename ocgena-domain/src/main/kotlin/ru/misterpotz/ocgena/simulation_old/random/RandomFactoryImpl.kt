package simulation.random

import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.utils.buildMutableMap
import javax.inject.Inject
import kotlin.random.Random

class RandomFactoryImpl @Inject constructor(
    simulationConfig: SimulationConfig,
    private val map: Map<@JvmSuppressWildcards RandomUseCase, @JvmSuppressWildcards Random>
) : RandomFactory {
    private val randomSeed: Int? = simulationConfig.randomSeed

    override fun create(): RandomSource {
        return RandomSourceImpl(
            buildMutableMap {
                for (i in RandomUseCase.entries) {
                    val chose = map[i] ?: randomSeed?.let { Random(randomSeed) }
                    ?: Random
                    put(i, chose)
                }
            }
        )
    }
}

interface RandomSource {
    fun tokenSelection(): Random
    fun timeSelection(): Random
    fun transitionSelection(): Random
    fun backwardSupport(): Random
}

enum class RandomUseCase {
    TOKEN_SELECTION,
    TIME_SELECTION,
    TRANSITION_SELECTION
}

class RandomSourceImpl(
    private val mutableMap: MutableMap<RandomUseCase, Random> = mutableMapOf()
) : RandomSource {
    override fun tokenSelection(): Random {
        return mutableMap[RandomUseCase.TOKEN_SELECTION]!!
    }

    override fun timeSelection(): Random {
        return mutableMap[RandomUseCase.TIME_SELECTION]!!
    }

    override fun transitionSelection(): Random {
        return mutableMap[RandomUseCase.TRANSITION_SELECTION]!!
    }

    override fun backwardSupport(): Random {
        return mutableMap[RandomUseCase.TOKEN_SELECTION]!!
    }

    companion object {
        fun forTokens(random: Random) : RandomSource {
            return RandomSourceImpl(mutableMapOf(RandomUseCase.TOKEN_SELECTION to random))
        }
    }
}