package ru.misterpotz.ocgena.utils

import kotlin.random.Random
import kotlin.random.nextInt

interface Randomizer {
    fun nextRandom(intRange: IntRange): Int
}

class ByRandomRandomizer(private val random: Random) : Randomizer {
    override fun nextRandom(intRange : IntRange): Int {
        return random.nextInt(intRange)
    }
}

class NoOpIteratorRandomizer() : Randomizer {
    private var counter = 0
    override fun nextRandom(intRange: IntRange): Int {
        return counter++.also {
            if (!intRange.contains(counter)) {
                counter = 0
            }
        }
    }
}
