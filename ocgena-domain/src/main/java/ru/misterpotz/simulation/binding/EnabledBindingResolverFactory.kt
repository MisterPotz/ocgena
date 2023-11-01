package simulation.binding

import model.typea.ArcMultiplicityTypeA
import model.typel.ExpressionArcMultiplicity
import ru.misterpotz.simulation.api.interactors.EnabledBindingResolverInteractor
import simulation.SimulationStateProvider
import simulation.typea.EnabledBindingTypeAResolverInteractor
import javax.inject.Inject
import javax.inject.Provider

class EnabledBindingResolverFactory @Inject constructor(
    private val enabledBindingTypeAResolver: Provider<EnabledBindingTypeAResolverInteractor>,
    private val stateProvider: SimulationStateProvider,
) {
    fun create(): EnabledBindingResolverInteractor {
        val arcMultiplicity = stateProvider.runningSimulatableOcNet().composedOcNet.arcMultiplicity

        return when (arcMultiplicity) {
            is ArcMultiplicityTypeA -> enabledBindingTypeAResolver.get()
            is ExpressionArcMultiplicity -> TODO("I.A.Lomazova specification is yet to be done")
            else -> throw IllegalArgumentException("type ${arcMultiplicity::class.simpleName} is not supported")
        }
    }
}
