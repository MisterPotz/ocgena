package model

class EnabledBindingCollectorVisitor : PetriAtomVisitor {

    private val recursionProtector = RecursionProtector()
    private val obtainedEnabledBindings : MutableList<Binding> = mutableListOf()

    fun clear() {
        obtainedEnabledBindings.clear()
    }

    fun getEnabledBindings() : List<Binding> {
        return obtainedEnabledBindings.toList()
    }

    override fun visitArc(arc: Arc) {
        recursionProtector.protectWithRecursionStack(arc) {
            arc.tailNode!!.acceptVisitor(this)
        }
    }

    override fun visitTransition(transition: Transition) {
        recursionProtector.protectWithRecursionStack(transition) {
            collectBinding(transition)
            for (outputArc in transition.outputArcs) {
                outputArc.requireArrowPlace().acceptVisitor(this)
            }
        }
    }

    private fun collectBinding(transition: Transition) {
        val enabledBinding = transition.getEnabledBinding()
        enabledBinding?.let { obtainedEnabledBindings.add(it) }
    }
    override fun visitPlace(place: Place) {
        recursionProtector.protectWithRecursionStack(place) {
            for (outputArc in place.outputArcs) {
                outputArc.requireArrowTransition().acceptVisitor(this)
            }
        }
    }
}
