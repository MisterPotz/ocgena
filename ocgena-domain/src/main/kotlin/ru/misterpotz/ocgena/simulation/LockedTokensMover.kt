package simulation

import ru.misterpotz.marking.objects.ObjectMarking
import ru.misterpotz.marking.transitions.TransitionInstance

abstract class LockedTokensMover {
    abstract fun tryFillOutputPlacesNormalArcs(
        transitionInstance: TransitionInstance,
//        transitionArcs: Arcs.WithTransitionGetter,
//        placeTyping: PlaceTyping,
//        inputPlaces: List<Place>,
//        outputPlaces: List<Place>,
//        inputMarking: ImmutableObjectMarking
    ): ObjectMarking
}
