package simulation.typea

import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.ocgena.collections.objects.ImmutableObjectMarking
import ru.misterpotz.simulation.api.interactors.TIOutputPlacesResolverInteractor
import ru.misterpotz.simulation.typea.OutputMarkingFillerTypeAFactory
import javax.inject.Inject



class TIOutputMarkingTypeAResolver @Inject constructor(
    private val outputMarkingFillerTypeAFactory: OutputMarkingFillerTypeAFactory
) : TIOutputPlacesResolverInteractor {

    override fun createOutputMarking(activeFiringTransition: TransitionInstance): ImmutableObjectMarking {
        val markingFillahr = outputMarkingFillerTypeAFactory.create(activeFiringTransition)

        return markingFillahr.fill()
    }
}
