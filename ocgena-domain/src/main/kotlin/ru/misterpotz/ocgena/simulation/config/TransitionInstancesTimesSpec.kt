package ru.misterpotz.ocgena.simulation.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

@Serializable
data class TransitionInstancesTimesSpec(
    @SerialName("default")
    val defaultTransitionTimeSpec: TransitionInstanceTimes = TransitionInstanceTimes(
        duration = Duration(10..10),
        timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(0..0)
    ),
    @SerialName("per_transition")
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
                }.toMutableMap()
            )
        }
    }
}
