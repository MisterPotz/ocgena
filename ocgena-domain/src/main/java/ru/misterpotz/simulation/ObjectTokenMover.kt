package simulation

import model.*
import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.ObjectMarking

abstract class ObjectTokenMover {
    abstract fun tryFillOutputPlacesNormalArcs(
        transitionArcs: Arcs.WithTransitionGetter,
        placeTyping: PlaceTyping,
        inputPlaces: List<Place>,
        outputPlaces: List<Place>,
        inputMarking: ImmutableObjectMarking
    ): ObjectMarking
}
