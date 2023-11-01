package ru.misterpotz.simulation.api.interactors

import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.marking.objects.ImmutableObjectMarking

interface TIOutputPlacesResolverInteractor {
    fun createOutputMarking(activeFiringTransition : TransitionInstance) : ImmutableObjectMarking
}
