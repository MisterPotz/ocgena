package ru.misterpotz.simulation.api.interactors

import simulation.binding.EnabledBinding

interface BindingSelectionInteractor {
    fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding
}