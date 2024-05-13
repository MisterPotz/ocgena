package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.ocgena.simulation_v2.input.SynchronizedArcGroup
import ru.misterpotz.ocgena.simulation_v2.utils.Ref

class MultiArcCondition(
    val syncTarget: TransitionWrapper,
    val index: Int,
    val arcs: Ref<List<InputArcWrapper>>,
    val originalCondition: SynchronizedArcGroup,
    val transitionWrapper: TransitionWrapper,
) : Comparable<MultiArcCondition> {

    val fromPlaces by lazy(LazyThreadSafetyMode.PUBLICATION) {
        arcs.ref.map { it.fromPlace }.sorted()
    }

    companion object {
        // Assuming TransitionWrapper has a sensible compareTo implementation
        val comparator: Comparator<MultiArcCondition> = compareBy(
            { it.transitionWrapper },
            { it.index },
            { it.syncTarget }
        )
    }

    override fun compareTo(other: MultiArcCondition): Int {
        return comparator.compare(this, other)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiArcCondition

        if (syncTarget != other.syncTarget) return false
        if (index != other.index) return false
        if (transitionWrapper != other.transitionWrapper) return false

        return true
    }


    override fun hashCode(): Int {
        var result = syncTarget.hashCode()
        result = 31 * result + index
        result = 31 * result + transitionWrapper.hashCode()
        return result
    }

    override fun toString(): String {
        return "MultiArcCondition(${
            fromPlaces.joinToString(
                ",",
                prefix = "[",
                postfix = "]"
            )
        }, syncTarget=$syncTarget, transition=${transitionWrapper.id})"
    }
}