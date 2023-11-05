package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

@Serializable
class TransitionInstancesTimesSpec(
    private val defaultTransitionTimeSpec: TransitionInstanceTimes?,
    private val transitionToTimeSpec: MutableMap<PetriAtomId, TransitionInstanceTimes> = mutableMapOf(),
) {
    operator fun get(transition: PetriAtomId): TransitionInstanceTimes {
        return transitionToTimeSpec[transition] ?: defaultTransitionTimeSpec!!
    }

    companion object {
        fun create(
            defaultTransitionInstanceTimes: TransitionInstanceTimes? = null,
            block: MutableMap<PetriAtomId, TransitionInstanceTimes>.() -> Unit): TransitionInstancesTimesSpec {
            return TransitionInstancesTimesSpec(
                defaultTransitionTimeSpec = defaultTransitionInstanceTimes,
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
