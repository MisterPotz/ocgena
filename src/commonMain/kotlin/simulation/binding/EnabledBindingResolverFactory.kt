package simulation.binding

import model.ArcMultiplicity
import model.Arcs
import model.aalst.ArcMultiplicityTypeA
import model.lomazova.ExpressionArcMultiplicity
import simulation.PMarkingProvider
import simulation.random.TokenSelector
import simulation.typea.EnabledBindingPlainResolver

class EnabledBindingResolverFactory(
    private val arcMultiplicity: ArcMultiplicity,
    private val arcs : Arcs,
    private val pMarkingProvider : PMarkingProvider,
    private val tokenSelector: TokenSelector,
) {
    fun create() : EnabledBindingResolver {
        return when (arcMultiplicity) {
            is ArcMultiplicityTypeA -> EnabledBindingPlainResolver(
                pMarkingProvider,
                arcMultiplicity,
                arcs = arcs,
                tokenSelector
            )
            is ExpressionArcMultiplicity -> TODO("I.A.Lomazova specification is yet to be done")
            else -> throw IllegalArgumentException("type ${arcMultiplicity::class.simpleName} is not supported")
        }
    }
}
