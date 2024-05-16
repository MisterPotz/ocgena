package ru.misterpotz.ocgena.simulation_old.interactors

import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.simulation_old.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation_old.binding.EnabledBindingWithTokens

interface EnabledBindingResolverInteractor {
    fun tryGetEnabledBinding(transition: Transition): EnabledBinding?
    fun requireEnabledBindingWithTokens(objectBinding: EnabledBinding): EnabledBindingWithTokens

}
