package ru.misterpotz.ocgena.simulation.state

import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionToInstancesMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionToTimeUntilInstanceAllowedMarking
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.simulation.structure.OcNetInstance

class State(override val petriAtomRegistry: PetriAtomRegistry) : OcNetInstance.State {
    override val tMarking: TransitionToInstancesMarking = TransitionToInstancesMarking()
    override val pMarking: PlaceToObjectMarking = PlaceToObjectMarking()
    override val tTimesMarking: TransitionToTimeUntilInstanceAllowedMarking = TransitionToTimeUntilInstanceAllowedMarking()
}
