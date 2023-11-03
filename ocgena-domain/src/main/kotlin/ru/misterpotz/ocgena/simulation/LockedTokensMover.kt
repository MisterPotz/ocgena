package ru.misterpotz.ocgena.simulation

import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionInstance

abstract class LockedTokensMover {
    abstract fun tryFillOutputPlacesNormalArcs(
        transitionInstance: TransitionInstance,
    ): PlaceToObjectMarking
}
