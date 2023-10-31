package simulation

import model.*
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.ObjectMarking

abstract class ObjectTokenMover {
    abstract fun tryFillOutputPlacesNormalArcs(
        transitionArcs: Arcs.WithTransitionGetter,
        placeTyping: PlaceTyping,
        inputPlaces: List<Place>,
        outputPlaces: List<Place>,
        inputMarking: ImmutableObjectMarking
    ): ObjectMarking
}
