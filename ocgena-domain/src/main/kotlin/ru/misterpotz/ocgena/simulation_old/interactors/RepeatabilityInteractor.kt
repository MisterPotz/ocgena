package ru.misterpotz.ocgena.simulation_old.interactors

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_old.ObjectTokenId
import ru.misterpotz.ocgena.simulation_old.binding.EnabledBinding
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