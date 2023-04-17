package model.utils

import model.AbsPetriAtomVisitorDFS
import model.Binding
import model.Transition

class EnabledBindingCollectorVisitorDFS() : AbsPetriAtomVisitorDFS() {

    private val obtainedEnabledBindings: MutableList<Binding> = mutableListOf()

    fun clear() {
        obtainedEnabledBindings.clear()
    }

    fun getEnabledBindings(): List<Binding> {
        return obtainedEnabledBindings.toList()
    }
    override fun doForTransitionBeforeDFS(transition: Transition): Boolean {
        collectBinding(transition)
        return false
    }

    private fun collectBinding(transition: Transition) {
        val enabledBinding = transition.getEnabledBinding()
        enabledBinding?.let { obtainedEnabledBindings.add(it) }
    }
}
