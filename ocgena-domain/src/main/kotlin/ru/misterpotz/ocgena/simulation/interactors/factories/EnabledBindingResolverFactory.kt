package ru.misterpotz.ocgena.simulation.interactors.factories

import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityTypeARegistry
import ru.misterpotz.ocgena.simulation.interactors.EnabledBindingResolverInteractor
import ru.misterpotz.ocgena.simulation.SimulationStateProvider
import ru.misterpotz.ocgena.simulation.typea.EnabledBindingTypeAResolverInteractor
import javax.inject.Inject
import javax.inject.Provider

class EnabledBindingResolverFactory @Inject constructor(
    private val enabledBindingTypeAResolver: Provider<EnabledBindingTypeAResolverInteractor>,
    private val stateProvider: SimulationStateProvider,
) {
    fun create(): EnabledBindingResolverInteractor {
        val arcMultiplicity = stateProvider.runningSimulatableOcNet().composedOcNet

        return when (arcMultiplicity) {
            is ArcToMultiplicityTypeARegistry -> enabledBindingTypeAResolver.get()
            else -> throw IllegalArgumentException("type ${arcMultiplicity::class.simpleName} is not supported")
        }
    }
}
