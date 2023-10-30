package simulation.binding

import model.ArcMultiplicity
import model.Arcs
import model.typea.ArcMultiplicityTypeA
import model.typel.ExpressionArcMultiplicity
import ru.misterpotz.simulation.marking.PMarkingProvider
import simulation.random.TokenSelector
import ru.misterpotz.simulation.transition.TransitionOccurrenceAllowedTimes
import simulation.typea.EnabledBindingTypeAResolver

class EnabledBindingResolverFactory(
    private val arcMultiplicity: ArcMultiplicity,
    private val arcs : Arcs,
    private val pMarkingProvider : PMarkingProvider,
    private val tokenSelector: TokenSelector,
    private val tTimes : TransitionOccurrenceAllowedTimes,
) {
    fun create() : EnabledBindingResolver {
        return when (arcMultiplicity) {
            is ArcMultiplicityTypeA -> EnabledBindingTypeAResolver(
                pMarkingProvider,
                arcMultiplicity,
                arcs = arcs,
                tokenSelector,
                tTimes = tTimes,
            )
            is ExpressionArcMultiplicity -> TODO("I.A.Lomazova specification is yet to be done")
            else -> throw IllegalArgumentException("type ${arcMultiplicity::class.simpleName} is not supported")
        }
    }
}
