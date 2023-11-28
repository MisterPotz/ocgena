package ru.misterpotz.ocgena.simulation.continuation

import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation.binding.EnabledBindingWithTokens

interface ExecutionContinuation {
    fun getEnabledBindings(): List<EnabledBinding>
    fun updatedEnabledBindings(enabledBindings: List<EnabledBinding>)
    suspend fun selectBinding(): EnabledBinding?
    suspend fun shouldDoNextStep(): Boolean
}

class NoOpExecutionContinuation : ExecutionContinuation {
    private var enabledBindings: List<EnabledBinding> = listOf()


    override fun updatedEnabledBindings(enabledBindings: List<EnabledBinding>) {
        this.enabledBindings = enabledBindings
    }

    override fun getEnabledBindings(): List<EnabledBinding> {
        return enabledBindings
    }

    override suspend fun selectBinding(): EnabledBinding? {
        return null
    }

    override suspend fun shouldDoNextStep(): Boolean {
        return true
    }
}

