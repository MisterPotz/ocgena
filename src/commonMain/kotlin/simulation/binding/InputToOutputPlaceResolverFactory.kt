package simulation.binding

import model.ArcMultiplicity
import model.Arcs
import model.aalst.StaticArcMultiplicity
import model.lomazova.ExpressionArcMultiplicity
import simulation.aalst.InputToOutputPlacePlainResolver

class InputToOutputPlaceResolverFactory(
    private val arcMultiplicity: ArcMultiplicity,
    private val arcs : Arcs,
) {
    fun create() : InputToOutputPlaceResolver {
        return when (arcMultiplicity) {
            is StaticArcMultiplicity -> InputToOutputPlacePlainResolver(arcMultiplicity, arcs = arcs)
            is ExpressionArcMultiplicity -> TODO("I.A.Lomazova specification is yet to be done")
            else -> throw IllegalArgumentException("type ${arcMultiplicity::class.simpleName} is not supported")
        }
    }
}
