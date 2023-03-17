package dsl

class TransitionDelegate(
    var lateAssignTransitionCreator: TransitionCreator? = null,
) : TransitionAcceptor {
    private val transitionCreator
        get() = lateAssignTransitionCreator!!

    override fun transition(block: OCTransitionScope.() -> Unit): TransitionDSL {
        return transitionCreator.creteTransition(label = null, block)
    }

    override fun transition(label: String, block: OCTransitionScope.() -> Unit): TransitionDSL {
        return transitionCreator.creteTransition(label = label, block)
    }

    override fun transition(label: String): TransitionDSL {
        return transitionCreator.creteTransition(label = label) { }
    }
}
