package ru.misterpotz.simulation.impl.interactors

import ru.misterpotz.simulation.api.interactors.BindingSelectionInteractor
import simulation.binding.EnabledBinding
import kotlin.random.Random

class BindingSelectionInteractorImpl(
    private val random: Random?,
) : BindingSelectionInteractor {
    override fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding {
        return random?.let { enabledBindings.random(random = it) } ?: enabledBindings.first()
    }
}

