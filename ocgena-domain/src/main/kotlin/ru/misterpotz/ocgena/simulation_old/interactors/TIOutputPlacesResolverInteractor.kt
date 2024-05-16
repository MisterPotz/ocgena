package ru.misterpotz.ocgena.simulation_old.interactors

import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.TransitionInstance

interface TIOutputPlacesResolverInteractor {
    fun createOutputMarking(activeFiringTransition : TransitionInstance) : ImmutablePlaceToObjectMarking
}
