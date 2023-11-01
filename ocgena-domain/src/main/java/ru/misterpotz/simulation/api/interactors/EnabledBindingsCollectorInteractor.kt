package ru.misterpotz.simulation.api.interactors

import simulation.SimulationStateProvider
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingResolverFactory
import simulation.binding.EnabledBindingWithTokens
import javax.inject.Inject

class EnabledBindingsCollectorInteractor @Inject constructor(
    simulationStateProvider: SimulationStateProvider,
    enabledBindingResolverFactory: EnabledBindingResolverFactory
) {
    private val transitions = simulationStateProvider.runningSimulatableOcNet().composedOcNet.coreOcNet.transitions

    private val enabledBindingResolver = enabledBindingResolverFactory.create()

    fun findEnabledBindings() : List<EnabledBinding> {
        return transitions.mapNotNull { transition ->
            enabledBindingResolver.tryGetEnabledBinding(transition)
        }
    }

    fun resolveEnabledObjectBinding(enabledObjectBinding: EnabledBinding): EnabledBindingWithTokens {
        return enabledBindingResolver.requireEnabledBindingWithTokens(enabledObjectBinding)
    }
}
