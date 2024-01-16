package ru.misterpotz.ocgena.simulation.state.original

import ru.misterpotz.ocgena.registries.original.TransitionToInstancesRegistryOriginal
import ru.misterpotz.ocgena.registries.original.TransitionToTimeUntilInstanceAllowedMarking
import ru.misterpotz.ocgena.registries.original.TransitionToTimeUntilInstanceAllowedRegistryOriginal
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation.state.SemanticsSpecificState

class CurrentSimulationStateOriginal : SemanticsSpecificState {
    override val type: SimulationSemanticsType = SimulationSemanticsType.ORIGINAL
    val tTimesMarking: TransitionToTimeUntilInstanceAllowedRegistryOriginal =
        TransitionToTimeUntilInstanceAllowedMarking()
    val tMarking: TransitionToInstancesRegistryOriginal = TransitionToInstancesRegistryOriginal()

}
