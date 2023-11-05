package ru.misterpotz.ocgena.simulation.interactors.typea

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation.interactors.TIOutputPlacesResolverInteractor
import ru.misterpotz.ocgena.simulation.typea.OutputMarkingFillerTypeAFactory
import javax.inject.Inject

class TIOutputPlaceToObjectMarkingTypeAResolver @Inject constructor(
    private val outputMarkingFillerTypeAFactory: OutputMarkingFillerTypeAFactory
) : TIOutputPlacesResolverInteractor {

    override fun createOutputMarking(activeFiringTransition: TransitionInstance): ImmutablePlaceToObjectMarking {
        val markingFillahr = outputMarkingFillerTypeAFactory.create(activeFiringTransition)

        return markingFillahr.fill()
    }
}
