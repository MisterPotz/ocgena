package ru.misterpotz.ocgena.simulation_old

import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.TransitionInstance

abstract class LockedTokensMover {
    abstract fun tryFillOutputPlacesFromLockedTokens(
        transitionInstance: TransitionInstance,
    ): PlaceToObjectMarking
}
