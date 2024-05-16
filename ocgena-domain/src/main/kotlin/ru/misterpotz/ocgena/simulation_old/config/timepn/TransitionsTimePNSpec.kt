package ru.misterpotz.ocgena.simulation_old.config.timepn

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_old.config.TransitionsSpec

@Serializable
@SerialName("time_pn")
data class TransitionsTimePNSpec(
    @SerialName("default")
    val default: TransitionTimePNTimes? = null,
    @SerialName("per_transition")
    val transitionToTimeSpec: MutableMap<PetriAtomId, TransitionTimePNTimes> = mutableMapOf(),
) : TransitionsSpec {
    fun getForTransition(transitionId : PetriAtomId) : TransitionTimePNTimes? {
        return transitionToTimeSpec[transitionId] ?: default
    }
}
