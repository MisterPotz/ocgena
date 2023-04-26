package simulation.lomazova

import model.Transition
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingResolver
import simulation.binding.EnabledBindingWithTokens

class EnabledBindingSubstitutableResolver(

) : EnabledBindingResolver {
    override fun tryGetEnabledBinding(transition: Transition): EnabledBinding? {
        TODO("Not yet implemented")
    }

    override fun requireEnabledBindingWithTokens(objectBinding: EnabledBinding): EnabledBindingWithTokens {
        TODO("Not yet implemented")
    }

    override fun checkBinding(objectBinding: EnabledBinding): Boolean {
        TODO("Not yet implemented")
    }
}
