package simulation.binding

import model.Transition

interface EnabledBindingResolver {
    fun tryGetEnabledBinding(transition: Transition): EnabledBinding?
    fun requireEnabledBindingWithTokens(objectBinding: EnabledBinding) : EnabledBindingWithTokens
    fun checkBinding(objectBinding: EnabledBinding) : Boolean
}
