package ru.misterpotz.ocgena.dsl

import dsl.TransitionCreator

open class TransitionDelegate(
    private val transitionCreator: TransitionCreator,
) : TransitionAcceptor {

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
