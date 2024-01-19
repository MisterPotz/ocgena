package ru.misterpotz.ocgena.utils

import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractorImpl
import java.lang.IllegalStateException

/*
 * guarantees that no repetitions will be given out
 * uses simple search fallback if can't find non-duplicate answer
 */
class RandomIterator(
    private val amount: Int,
    private val randomizer: Randomizer,
) : Iterator<Int> {
    val attemptsSet = sortedSetOf<Int>()
    val range = 0..<amount
    override fun hasNext(): Boolean {
        return attemptsSet.size < amount
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
        while (guardCounter < TokenSelectionInteractorImpl.GUARD_MULTIPLIER * amount) {
            attempt = randomizer.nextRandom(range)
            if (attempt !in attemptsSet) {
                attemptsSet.add(attempt)
                return attempt
            } else {
                guardCounter++
            }
        }
        // try generate with heuristics
        attempt = randomizer.nextRandom(range)

        val checkingRangeLeft = attempt.downTo(0)
        for (i in checkingRangeLeft) {
            if (checkValueAndReturn(i)) return i
        }
        val checkingRangeRight = (attempt + 1)..<amount
        for (i in checkingRangeRight) {
            if (checkValueAndReturn(i)) return i
        }

        throw IllegalStateException("hi, random iterator doesn't feel well")
    }

}