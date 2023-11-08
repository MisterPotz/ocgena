package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

@Serializable
data class TransitionInstancesTimesSpec(
    val defaultTransitionTimeSpec: TransitionInstanceTimes = TransitionInstanceTimes(
        duration = Duration(10..10),
        timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(0..0)
    ),
    val transitionToTimeSpec: MutableMap<PetriAtomId, TransitionInstanceTimes> = mutableMapOf(),
) {
    operator fun get(transition: PetriAtomId): TransitionInstanceTimes {
        return transitionToTimeSpec[transition] ?: defaultTransitionTimeSpec
    }

    companion object {
        fun create(
            defaultTransitionInstanceTimes: TransitionInstanceTimes = TransitionInstanceTimes(
                duration = Duration(10..10),
                timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(0..0)
            ),
            block: MutableMap<PetriAtomId, TransitionInstanceTimes>.() -> Unit
        ): TransitionInstancesTimesSpec {
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
