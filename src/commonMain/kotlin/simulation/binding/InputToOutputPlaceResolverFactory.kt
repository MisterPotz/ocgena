package simulation.binding

import model.ArcMultiplicity
import model.Arcs
import model.aalst.ArcMultiplicityTypeA
import model.lomazova.ExpressionArcMultiplicity
import simulation.typea.InputToOutputPlacePlainResolver

class InputToOutputPlaceResolverFactory(
    private val arcMultiplicity: ArcMultiplicity,
    private val arcs : Arcs,
) {
    fun create() : InputToOutputPlaceResolver {
        return when (arcMultiplicity) {
            is ArcMultiplicityTypeA -> InputToOutputPlacePlainResolver(arcMultiplicity, arcs = arcs)
            is ExpressionArcMultiplicity -> TODO("I.A.Lomazova specification is yet to be done")
            else -> throw IllegalArgumentException("type ${arcMultiplicity::class.simpleName} is not supported")
        }
    }
}
