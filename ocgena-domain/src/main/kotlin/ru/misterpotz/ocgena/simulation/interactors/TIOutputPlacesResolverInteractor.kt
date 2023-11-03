package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionInstance

interface TIOutputPlacesResolverInteractor {
    fun createOutputMarking(activeFiringTransition : TransitionInstance) : ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking
}
