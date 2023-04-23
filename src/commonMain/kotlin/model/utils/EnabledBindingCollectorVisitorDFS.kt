package model.utils

import model.AbsPetriAtomVisitorDFS
import model.EnabledSimpleBinding
import model.Transition

class EnabledBindingCollectorVisitorDFS() : AbsPetriAtomVisitorDFS() {

    private val obtainedEnabledBindings: MutableList<EnabledSimpleBinding> = mutableListOf()

    fun fullReset() {
        clear()
        cleanStack()
    }
    fun clear() {
        obtainedEnabledBindings.clear()
    }

    fun getEnabledBindings(): List<EnabledSimpleBinding> {
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
