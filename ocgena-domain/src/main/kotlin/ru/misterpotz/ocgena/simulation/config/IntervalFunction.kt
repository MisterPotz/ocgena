package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

@Serializable
data class SerializableIntervalFunction(
    val defaultTransitionTimes: SerializableTransitionTimes?,
    val transitionToFiringTime: Map<PetriAtomId, SerializableTransitionTimes> = mutableMapOf(),
)

class IntervalFunction(
    private val defaultTransitionTimes: TransitionTimes?,
    private val transitionToFiringTime: MutableMap<PetriAtomId, TransitionTimes> = mutableMapOf(),
) {
    operator fun get(transition: PetriAtomId): TransitionTimes {
        return transitionToFiringTime[transition] ?: defaultTransitionTimes!!
    }

    fun toSerializable(): SerializableIntervalFunction {
        return SerializableIntervalFunction(
            defaultTransitionTimes = defaultTransitionTimes?.serializable,
            transitionToFiringTime = buildMap {
                for (i in transitionToFiringTime.keys) {
                    put(i, transitionToFiringTime[i]!!.serializable)
                }
            }
        )
    }

    companion object {
        fun create(
            defaultTransitionTimes: TransitionTimes? = null,
            block: MutableMap<PetriAtomId, TransitionTimes>.() -> Unit): IntervalFunction {
            return IntervalFunction(
                defaultTransitionTimes = defaultTransitionTimes,
                buildMap {
                    block()
                }.toList().fold(mutableMapOf()) { accum, entry ->
                    accum[entry.first] = entry.second
                    accum
                }
            )
        }
    }
}
