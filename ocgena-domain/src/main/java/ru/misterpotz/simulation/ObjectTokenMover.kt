package simulation

import model.*
import ru.misterpotz.model.ImmutableObjectMarking
import ru.misterpotz.model.ObjectMarking

abstract class ObjectTokenMover {
    abstract fun tryFillOutputPlacesNormalArcs(
        transitionArcs: Arcs.WithTransitionGetter,
        placeTyping: PlaceTyping,
        inputPlaces: List<Place>,
        outputPlaces: List<Place>,
        inputMarking: ImmutableObjectMarking
    ): ObjectMarking
}
