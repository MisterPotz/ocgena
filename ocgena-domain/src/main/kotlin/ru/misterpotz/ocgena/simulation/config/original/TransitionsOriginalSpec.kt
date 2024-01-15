package ru.misterpotz.ocgena.simulation.config.original

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.config.TransitionsSpec

@Serializable
data class TransitionsOriginalSpec(
    @SerialName("default")
    val defaultTransitionTimeSpec: TransitionInstanceTimes = TransitionInstanceTimes(
        duration = Duration(10..10),
        timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(0..0)
    ),
    @SerialName("per_transition")
    val transitionToTimeSpec: MutableMap<PetriAtomId, TransitionInstanceTimes> = mutableMapOf(),
) : TransitionsSpec {
    operator fun get(transition: PetriAtomId): TransitionInstanceTimes {
        return transitionToTimeSpec[transition] ?: defaultTransitionTimeSpec
    }

    fun update(block: MutableMap<PetriAtomId, TransitionInstanceTimes>.() -> Unit): TransitionsOriginalSpec {
        val updatedMap = transitionToTimeSpec.toMutableMap().apply(block)
        return copy(transitionToTimeSpec = updatedMap)
    }

    companion object {
        fun create(
            defaultTransitionInstanceTimes: TransitionInstanceTimes = TransitionInstanceTimes(
                duration = Duration(10..10),
                timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(0..0)
            ),
            block: MutableMap<PetriAtomId, TransitionInstanceTimes>.() -> Unit
        ): TransitionsOriginalSpec {
            return TransitionsOriginalSpec(
                defaultTransitionTimeSpec = defaultTransitionInstanceTimes,
                buildMap {
                    block()
                }.toMutableMap()
            )
        }
    }
}
