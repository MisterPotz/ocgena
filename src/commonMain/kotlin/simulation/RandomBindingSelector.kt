package simulation

import simulation.binding.EnabledBinding

class RandomBindingSelector(
) {
    fun selectBinding(enabledBindings : List<EnabledBinding>) : EnabledBinding {
        return enabledBindings.random()
    }
}
