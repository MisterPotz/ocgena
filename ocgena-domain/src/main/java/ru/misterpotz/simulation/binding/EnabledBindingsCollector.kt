package simulation.binding

import simulation.SimulationStateProvider
import javax.inject.Inject

class EnabledBindingsCollector @Inject constructor(
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
