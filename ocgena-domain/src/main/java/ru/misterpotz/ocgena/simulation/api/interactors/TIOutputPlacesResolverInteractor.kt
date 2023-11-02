package ru.misterpotz.ocgena.simulation.api.interactors

import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.ocgena.collections.objects.ImmutableObjectMarking

interface TIOutputPlacesResolverInteractor {
    fun createOutputMarking(activeFiringTransition : TransitionInstance) : ImmutableObjectMarking
}
