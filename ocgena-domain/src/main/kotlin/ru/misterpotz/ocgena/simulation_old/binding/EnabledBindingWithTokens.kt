package ru.misterpotz.ocgena.simulation_old.binding

import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_old.Time

class EnabledBindingWithTokens(
    val transition: PetriAtomId,
    val involvedObjectTokens: ImmutablePlaceToObjectMarking,
) {
    val synchronizationTime = calculateSynchronizationTime()

    private fun calculateSynchronizationTime(): Time {
        return 0
    }
}
