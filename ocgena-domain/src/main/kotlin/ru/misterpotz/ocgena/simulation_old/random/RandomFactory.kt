package simulation.random

import kotlin.random.Random

interface RandomFactory {
    fun create(): RandomSource
}