package ru.misterpotz.ocgena.utils

import ru.misterpotz.ocgena.simulation_old.interactors.TokenSelectionInteractorImpl
import java.lang.IllegalStateException

/*
 * guarantees that no repetitions will be given out
 * uses simple search fallback if can't find non-duplicate answer
 */
class RandomIterator(
    private val amountToGenerate : Int,
    private val rangeToSelectFrom : IntRange,
    private val randomizer: Randomizer,
) : Iterator<Int> {
    val attemptsSet = sortedSetOf<Int>()
    override fun hasNext(): Boolean {
        return attemptsSet.size < amountToGenerate
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
        while (guardCounter < TokenSelectionInteractorImpl.GUARD_MULTIPLIER * amountToGenerate) {
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

        val checkingRangeLeft = attempt.downTo(rangeToSelectFrom.first)
        for (i in checkingRangeLeft) {
            if (checkValueAndReturn(i)) return i
        }
        val checkingRangeRight = (attempt + 1)..rangeToSelectFrom.last
        for (i in checkingRangeRight) {
            if (checkValueAndReturn(i)) return i
        }

        throw IllegalStateException("hi, random iterator doesn't feel well")
    }
}