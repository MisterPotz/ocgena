package ru.misterpotz.ocgena.utils

import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractorImpl
import java.lang.IllegalStateException

/*
 * guarantees that no repetitions will be given out
 * uses simple search fallback if can't find non-duplicate answer
 */
class RandomIterator(
    private val size : Int,
    private val rangeToSelectFrom : IntRange,
    private val randomizer: Randomizer,
) : Iterator<Int> {
    val attemptsSet = sortedSetOf<Int>()
    override fun hasNext(): Boolean {
        return attemptsSet.size < size
    }

    private fun checkValueAndReturn(i : Int) : Boolean {
        if (i !in attemptsSet) {
            attemptsSet.add(i)
            return true
        }
        return false
    }

    override fun next(): Int {
        var attempt: Int?

        var guardCounter : Int = 0


        // try generate truly randomly
        while (guardCounter < TokenSelectionInteractorImpl.GUARD_MULTIPLIER * size) {
            attempt = randomizer.nextRandom(rangeToSelectFrom)
            if (attempt !in attemptsSet) {
                attemptsSet.add(attempt)
                return attempt
            } else {
                guardCounter++
            }
        }
        // try generate with heuristics
        attempt = randomizer.nextRandom(rangeToSelectFrom)

        val checkingRangeLeft = attempt.downTo(0)
        for (i in checkingRangeLeft) {
            if (checkValueAndReturn(i)) return i
        }
        val checkingRangeRight = (attempt + 1)..<size
        for (i in checkingRangeRight) {
            if (checkValueAndReturn(i)) return i
        }

        throw IllegalStateException("hi, random iterator doesn't feel well")
    }
}