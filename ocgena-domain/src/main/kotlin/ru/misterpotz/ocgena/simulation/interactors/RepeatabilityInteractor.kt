package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.atoms.TransitionId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import sun.tools.jstat.Token
import javax.inject.Inject

class RepeatabilityInteractor @Inject constructor() {
    fun sortPlaces(places: List<PetriAtomId>): List<PetriAtomId> {
        return places.sorted()
    }

    fun sortTokens(objectTokens: List<ObjectTokenId>): List<ObjectTokenId> = objectTokens.sorted()

    fun ensureEnabledBindingsSorted(enabledBindingsList: List<EnabledBinding>): List<EnabledBinding> {
        return enabledBindingsList.sortedBy { it.transition.id }
    }
}