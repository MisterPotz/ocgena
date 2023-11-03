package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation.binding.EnabledBindingWithTokens

interface EnabledBindingResolverInteractor {
    fun tryGetEnabledBinding(transition: Transition): EnabledBinding?
    fun requireEnabledBindingWithTokens(objectBinding: EnabledBinding) : EnabledBindingWithTokens
}
