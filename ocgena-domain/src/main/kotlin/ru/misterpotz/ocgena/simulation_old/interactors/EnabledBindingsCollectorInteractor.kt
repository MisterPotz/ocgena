package ru.misterpotz.ocgena.simulation_old.interactors

import ru.misterpotz.ocgena.simulation_old.SimulationStateProvider
import ru.misterpotz.ocgena.simulation_old.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation_old.binding.EnabledBindingWithTokens
import ru.misterpotz.ocgena.simulation_old.di.SimulationScope
import ru.misterpotz.ocgena.simulation_old.interactors.factories.EnabledBindingResolverFactory
import ru.misterpotz.ocgena.utils.buildSortedList
import javax.inject.Inject

@SimulationScope
class EnabledBindingsCollectorInteractor @Inject constructor(
    simulationStateProvider: SimulationStateProvider,
    enabledBindingResolverFactory: EnabledBindingResolverFactory,
    private val repeatabilityInteractor: RepeatabilityInteractor,
) {
    private val transitions = simulationStateProvider.simulatableOcNetInstance().transitionsRegistry

    private val enabledBindingResolver = enabledBindingResolverFactory.create()

    fun findEnabledBindings() : List<EnabledBinding> {
        val enabledBindingSortedList = buildSortedList {
            for (transition in transitions.iterable) {
                val enabledBinding = enabledBindingResolver.tryGetEnabledBinding(transition)
                if (enabledBinding != null) {
                    add(enabledBinding)
                }
            }
        }
        return repeatabilityInteractor.ensureEnabledBindingsSorted(enabledBindingSortedList)
    }

    fun resolveEnabledObjectBinding(enabledObjectBinding: EnabledBinding): EnabledBindingWithTokens {
        return enabledBindingResolver.requireEnabledBindingWithTokens(enabledObjectBinding)
    }
}
