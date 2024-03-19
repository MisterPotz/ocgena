package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import simulation.random.RandomSource
import javax.inject.Inject
import kotlin.random.Random

interface BindingSelectionInteractor {
    fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding
}

class BindingSelectionInteractorImpl @Inject constructor(
    private val randomSource: RandomSource,
) : BindingSelectionInteractor {
    override fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding {
        return randomSource.backwardSupport().let { enabledBindings.random(random = it) }
    }
}

