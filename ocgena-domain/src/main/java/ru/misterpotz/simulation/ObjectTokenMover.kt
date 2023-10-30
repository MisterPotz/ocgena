package simulation

import model.*

abstract class ObjectTokenMover {
    abstract fun tryFillOutputPlacesNormalArcs(
        transitionArcs: Arcs.WithTransitionGetter,
        placeTyping: PlaceTyping,
        inputPlaces: List<Place>,
        outputPlaces: List<Place>,
        inputMarking: ImmutableObjectMarking
    ): ObjectMarking
}
