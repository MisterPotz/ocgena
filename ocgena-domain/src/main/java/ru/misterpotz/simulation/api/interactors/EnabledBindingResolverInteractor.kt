package ru.misterpotz.simulation.api.interactors

import model.Transition
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingWithTokens

interface EnabledBindingResolverInteractor {
    fun tryGetEnabledBinding(transition: Transition): EnabledBinding?
    fun requireEnabledBindingWithTokens(objectBinding: EnabledBinding) : EnabledBindingWithTokens
}
