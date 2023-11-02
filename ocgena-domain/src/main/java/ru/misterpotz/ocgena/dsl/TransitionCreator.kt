package dsl

class TransitionCreator(
    private val transitionContainer: TransitionContainer,
) {
    private val transitions: MutableMap<String, TransitionDSL>
        get() = transitionContainer.transitions
    private val transitionPatternIdCreator
        get() = transitionContainer.transitionPatternIdCreator

    private fun findExistingTransition(label: String?): TransitionDSL? {
        return transitions[label ?: return null]
    }

    fun creteTransition(label: String?, block: OCTransitionScope.() -> Unit): TransitionDSL {
        findExistingTransition(label)?.let { return it }

        val defaultId = transitionPatternIdCreator.newLabelId()

        val transitionDSLImpl = TransitionDSLImpl(
            transitionIndex = transitionPatternIdCreator.lastIntId,
            defaultLabel = label ?: transitionPatternIdCreator.lastLabelId,
        )
        transitionDSLImpl.block()

        transitions[label ?: defaultId] = transitionDSLImpl
        return transitionDSLImpl
    }
}
