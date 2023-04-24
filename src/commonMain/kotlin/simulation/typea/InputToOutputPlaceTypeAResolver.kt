package simulation.typea

import model.ActiveFiringTransition
import model.Arcs
import model.ObjectMarking
import model.aalst.ArcMultiplicityTypeA
import simulation.binding.InputToOutputPlaceResolver

class InputToOutputPlaceTypeAResolver(
    private val arcMultiplicity: ArcMultiplicityTypeA,
    val arcs : Arcs,
) : InputToOutputPlaceResolver {

    override fun createOutputMarking(activeFiringTransition: ActiveFiringTransition): ObjectMarking {
        val markingFillahr = OutputMarkingFiller(activeFiringTransition, arcs, arcMultiplicity)
        return markingFillahr.fill()
    }
}
