package simulation.binding

import model.ArcMultiplicity
import model.Arcs
import model.aalst.StaticArcMultiplicity
import model.lomazova.ExpressionArcMultiplicity
import simulation.PMarkingProvider
import simulation.aalst.EnabledBindingPlainResolver

class EnabledBindingResolverFactory(
    private val arcMultiplicity: ArcMultiplicity,
    private val arcs : Arcs,
    private val pMarkingProvider : PMarkingProvider
) {
    fun create() : EnabledBindingResolver {
        return when (arcMultiplicity) {
            is StaticArcMultiplicity -> EnabledBindingPlainResolver(pMarkingProvider, arcMultiplicity, arcs = arcs)
            is ExpressionArcMultiplicity -> TODO("I.A.Lomazova specification is yet to be done")
            else -> throw IllegalArgumentException("type ${arcMultiplicity::class.simpleName} is not supported")
        }
    }
}
