package ru.misterpotz.ocgena.registries.original

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time

interface TransitionToTimeUntilInstanceAllowedRegistryOriginal {
    val keys : Iterable<PetriAtomId>
    fun increaseSimTime(time: Time)
    fun earliestNonZeroTime(): Time?
    fun isAllowedToBeEnabled(transition: PetriAtomId): Boolean
    fun getNextAllowedTime(transition: PetriAtomId) : Time?
    fun setNextAllowedTime(transition: PetriAtomId, time: Time)
}

fun TransitionToTimeUntilInstanceAllowedMarking(transitionsToNextTimes: MutableMap<PetriAtomId, Time> = mutableMapOf())
        : TransitionToTimeUntilInstanceAllowedRegistryOriginal {
    return TransitionToTimeUntilInstanceAllowedRegistryOriginalMap(transitionsToNextTimes)
}

internal class TransitionToTimeUntilInstanceAllowedRegistryOriginalMap(
    private val transitionsToNextTimes: MutableMap<PetriAtomId, Time> = mutableMapOf()
) :
    TransitionToTimeUntilInstanceAllowedRegistryOriginal {
    override val keys: Iterable<PetriAtomId>
        get() = transitionsToNextTimes.keys

    override fun increaseSimTime(time: Time) {
        for (i in transitionsToNextTimes.keys) {
            val currentValue = transitionsToNextTimes[i]!!

            transitionsToNextTimes[i] = (currentValue - time).coerceAtLeast(0)
        }
    }

    override fun earliestNonZeroTime(): Time? {
        return transitionsToNextTimes.values.fold(null) { accum: Time?, right: Time ->
            if (right > 0 || (accum != null && accum > right)) {
                right
            } else {
                accum
            }
        }
    }
    
    override fun isAllowedToBeEnabled(transition: PetriAtomId): Boolean {
        return transitionsToNextTimes[transition] == 0L
    }

    override fun getNextAllowedTime(transition: PetriAtomId): Time? {
        return transitionsToNextTimes[transition]
    }

    override fun setNextAllowedTime(transition: PetriAtomId, time: Time) {
        transitionsToNextTimes[transition] = time
    }
}
