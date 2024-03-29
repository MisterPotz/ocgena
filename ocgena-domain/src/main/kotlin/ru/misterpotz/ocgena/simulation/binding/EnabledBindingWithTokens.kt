package ru.misterpotz.ocgena.simulation.binding

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time

class EnabledBindingWithTokens(
    val transition: PetriAtomId,
    val involvedObjectTokens: ImmutablePlaceToObjectMarking,
) {
    val synchronizationTime = calculateSynchronizationTime()

    private fun calculateSynchronizationTime(): Time {
        return 0
    }
}
