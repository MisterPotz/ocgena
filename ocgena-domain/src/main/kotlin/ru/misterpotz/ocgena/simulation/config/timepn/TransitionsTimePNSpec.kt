package ru.misterpotz.ocgena.simulation.config.timepn

import kotlinx.serialization.SerialName
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.config.TransitionsSpec
import ru.misterpotz.ocgena.simulation.config.original.Duration

class TransitionsTimePNSpec(
    @SerialName("default")
    val default: TransitionTimePNTimes = TransitionTimePNTimes(
        duration = Duration(10..10),
    ),
    @SerialName("per_transition")
    val transitionToTimeSpec: MutableMap<PetriAtomId, TransitionTimePNTimes> = mutableMapOf(),
) : TransitionsSpec