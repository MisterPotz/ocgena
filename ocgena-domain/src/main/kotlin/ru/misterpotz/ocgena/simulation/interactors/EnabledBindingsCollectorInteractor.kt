package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.simulation.SimulationStateProvider
import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation.interactors.factories.EnabledBindingResolverFactory
import ru.misterpotz.ocgena.simulation.binding.EnabledBindingWithTokens
import javax.inject.Inject

class EnabledBindingsCollectorInteractor @Inject constructor(
    simulationStateProvider: SimulationStateProvider,
    enabledBindingResolverFactory: EnabledBindingResolverFactory
) {
    private val transitions = simulationStateProvider.runningSimulatableOcNet().composedOcNet.ocNet.transitionsRegistry

    private val enabledBindingResolver = enabledBindingResolverFactory.create()

    fun findEnabledBindings() : List<EnabledBinding> {
        return transitions.iterable.mapNotNull { transition ->
            enabledBindingResolver.tryGetEnabledBinding(transition)
        }
    }

    fun resolveEnabledObjectBinding(enabledObjectBinding: EnabledBinding): EnabledBindingWithTokens {
        return enabledBindingResolver.requireEnabledBindingWithTokens(enabledObjectBinding)
    }
}
