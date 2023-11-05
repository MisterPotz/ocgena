package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.TransitionInstance

interface TIOutputPlacesResolverInteractor {
    fun createOutputMarking(activeFiringTransition : TransitionInstance) : ImmutablePlaceToObjectMarking
}
