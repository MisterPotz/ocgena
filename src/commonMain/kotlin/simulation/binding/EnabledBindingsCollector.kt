package simulation.binding

import model.Transition

class EnabledBindingsCollector(
    val transitions: List<Transition>,
    val enabledBindingResolverFactory: EnabledBindingResolverFactory
) {
    private val enabledBindingResolver = enabledBindingResolverFactory.create()

    fun findEnabledBindings() : List<EnabledBinding> {
        return transitions.mapNotNull {
            enabledBindingResolver.tryGetEnabledBinding(it)
        }
    }

    fun resolveEnabledObjectBinding(enabledObjectBinding: EnabledBinding): EnabledBindingWithTokens {
        return enabledBindingResolver.requireEnabledBindingWithTokens(enabledObjectBinding)
    }
}
