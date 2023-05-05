package simulation.binding

import model.ArcMultiplicity
import model.Arcs
import model.PlaceTyping
import model.typea.ArcMultiplicityTypeA
import model.typel.ExpressionArcMultiplicity
import simulation.typea.InputToOutputPlaceTypeAResolver

class InputToOutputPlaceResolverFactory(
    private val arcMultiplicity: ArcMultiplicity,
    private val arcs : Arcs,
    private val placeTyping: PlaceTyping
) {
    fun create() : InputToOutputPlaceResolver {
        return when (arcMultiplicity) {
            is ArcMultiplicityTypeA -> InputToOutputPlaceTypeAResolver(
                arcMultiplicity,
                arcs = arcs,
                placeTyping = placeTyping)
            is ExpressionArcMultiplicity -> TODO("I.A.Lomazova specification is yet to be done")
            else -> throw IllegalArgumentException("type ${arcMultiplicity::class.simpleName} is not supported")
        }
    }
}
