package ru.misterpotz.ocgena.simulation

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.TransitionInstance

abstract class LockedTokensMover {
    abstract fun tryFillOutputPlacesFromLockedTokens(
        transitionInstance: TransitionInstance,
    ): PlaceToObjectMarking
}
