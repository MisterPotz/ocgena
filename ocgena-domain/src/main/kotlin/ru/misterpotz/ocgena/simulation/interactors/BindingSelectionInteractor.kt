package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import kotlin.random.Random

interface BindingSelectionInteractor {
    fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding
}

class BindingSelectionInteractorImpl(
    private val random: Random?,
) : BindingSelectionInteractor {
    override fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding {
        return random?.let { enabledBindings.random(random = it) } ?: enabledBindings.first()
    }
}

