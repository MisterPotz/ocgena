package simulation.typea

import model.ActiveFiringTransition
import model.Arcs
import model.ImmutableObjectMarking
import model.PlaceTyping
import model.aalst.ArcMultiplicityTypeA
import simulation.binding.InputToOutputPlaceResolver

class InputToOutputPlaceTypeAResolver(
    private val arcMultiplicity: ArcMultiplicityTypeA,
    val arcs : Arcs,
    private val placeTyping: PlaceTyping,
) : InputToOutputPlaceResolver {

    override fun createOutputMarking(activeFiringTransition: ActiveFiringTransition): ImmutableObjectMarking {
        val markingFillahr = OutputMarkingFillerTypeA(
            activeFiringTransition,
            arcs,
            arcMultiplicity,
            placeTyping = placeTyping
        )
        return markingFillahr.fill()
    }
}
