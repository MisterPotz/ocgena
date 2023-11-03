package ru.misterpotz.ocgena.simulation.interactors

import simulation.SimulationStateProvider
import simulation.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation.interactors.factories.EnabledBindingResolverFactory
import simulation.binding.EnabledBindingWithTokens
import javax.inject.Inject

class EnabledBindingsCollectorInteractor @Inject constructor(
    simulationStateProvider: SimulationStateProvider,
    enabledBindingResolverFactory: EnabledBindingResolverFactory
) {
    private val transitions = simulationStateProvider.runningSimulatableOcNet().composedOcNet.coreOcNet.transitionsRegistry

    private val enabledBindingResolver = enabledBindingResolverFactory.create()

    fun findEnabledBindings() : List<EnabledBinding> {
        return transitions.iterable.map { transition ->
            enabledBindingResolver.tryGetEnabledBinding(transition)
        }
    }

    fun resolveEnabledObjectBinding(enabledObjectBinding: EnabledBinding): EnabledBindingWithTokens {
        return enabledBindingResolver.requireEnabledBindingWithTokens(enabledObjectBinding)
    }
}
