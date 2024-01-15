package ru.misterpotz.ocgena.simulation.state.original

import ru.misterpotz.ocgena.registries.original.TransitionToInstancesRegistryOriginal
import ru.misterpotz.ocgena.registries.original.TransitionToTimeUntilInstanceAllowedMarking
import ru.misterpotz.ocgena.registries.original.TransitionToTimeUntilInstanceAllowedRegistryOriginal

class CurrentSimulationStateOriginal {
    val tTimesMarking: TransitionToTimeUntilInstanceAllowedRegistryOriginal =
        TransitionToTimeUntilInstanceAllowedMarking()
    val tMarking: TransitionToInstancesRegistryOriginal = TransitionToInstancesRegistryOriginal()

}