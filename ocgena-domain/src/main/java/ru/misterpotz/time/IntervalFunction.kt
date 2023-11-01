package model.time

import kotlinx.serialization.Serializable
import ru.misterpotz.model.atoms.TransitionId

@Serializable
data class SerializableIntervalFunction(
    val defaultTransitionTimes: SerializableTransitionTimes?,
    val transitionToFiringTime: Map<TransitionId, SerializableTransitionTimes> = mutableMapOf(),
)

class IntervalFunction(
    private val defaultTransitionTimes: TransitionTimes?,
    private val transitionToFiringTime: MutableMap<TransitionId, TransitionTimes> = mutableMapOf(),
) {
    operator fun get(transition: TransitionId): TransitionTimes {
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
            block: MutableMap<TransitionId, TransitionTimes>.() -> Unit): IntervalFunction {
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
