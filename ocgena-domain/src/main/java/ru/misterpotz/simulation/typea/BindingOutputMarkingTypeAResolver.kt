package simulation.typea

import model.ActiveFiringTransition
import model.Arcs
import ru.misterpotz.model.ImmutableObjectMarking
import model.PlaceTyping
import simulation.ObjectTokenGenerator
import simulation.ObjectTokenMoverFactory
import simulation.OutputMarkingFiller
import simulation.binding.InputToOutputPlaceResolver

class BindingOutputMarkingTypeAResolver(
    val arcs: Arcs,
    private val placeTyping: PlaceTyping,
    private val objectTokenGenerator: ObjectTokenGenerator,
    private val objectTokenMoverFactory: ObjectTokenMoverFactory
) : InputToOutputPlaceResolver {

    override fun createOutputMarking(activeFiringTransition: ActiveFiringTransition): ImmutableObjectMarking {
        val markingFillahr = OutputMarkingFiller(
            activeFiringTransition,
            arcs = arcs,
            placeTyping = placeTyping,
            objectTokenGenerator = objectTokenGenerator,
            objectTokenMoverFactory = objectTokenMoverFactory
        )
        return markingFillahr.fill()
    }
}
