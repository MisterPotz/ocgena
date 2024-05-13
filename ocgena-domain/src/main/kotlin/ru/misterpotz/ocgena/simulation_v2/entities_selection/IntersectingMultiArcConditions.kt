package ru.misterpotz.ocgena.simulation_v2.entities_selection

import ru.misterpotz.ocgena.simulation_v2.entities.MultiArcCondition
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.RandomLeveledCombinationIterator
import ru.misterpotz.ocgena.simulation_v2.entities.InputArcWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.utils.Randomizer
import java.util.SortedSet

fun List<InputArcWrapper>.makePreplaceTokenRequrements(): List<Int> {
    return map { it.consumptionSpec.tokenRequirement() }
}

fun List<InputArcWrapper>.orderedMapToPreplaces(): List<PlaceWrapper> {
    return map { it.fromPlace }
}

class IntersectingMultiArcConditions(
    val conditions: SortedSet<MultiArcCondition>,
    val transition: TransitionWrapper,
) : Comparable<IntersectingMultiArcConditions> {
    val relatedInputArcs by lazy(LazyThreadSafetyMode.NONE) {
        conditions.flatMap { it.arcs.ref }.toSet()
    }

    fun conditionArcSortedByStrongestDescending(): List<InputArcWrapper> {
        val maxConditions = relatedInputArcs.maxOf { it.underConditions.size }

        relatedInputArcs.filter { it.underConditions.size == maxConditions }

        return relatedInputArcs.sortedWith(InputArcWrapper.ByConditionSizeByArcSpecByTransitionEntries)
    }

    fun makeRandomCombinationIterator(
        tokenSlice: TokenSlice,
        randomizer: Randomizer
    ): RandomLeveledCombinationIterator {
        val sortedArcs = conditionArcSortedByStrongestDescending()
        val tokenRequirements = sortedArcs.makePreplaceTokenRequrements()
        val orderedPreplaces = sortedArcs.orderedMapToPreplaces()

        val randomLeveledCombinationIterator = RandomLeveledCombinationIterator(
            orderedPreplaces.map { it.tokenRangeAt(tokenSlice) },
            tokenRequirements,
            randomizer,
            mode = RandomLeveledCombinationIterator.Mode.BFS,
        )
        return randomLeveledCombinationIterator
    }

    override fun compareTo(other: IntersectingMultiArcConditions): Int {
        return comparator.compare(this, other)
    }

    companion object {
        val comparator = compareBy<IntersectingMultiArcConditions>(
            {
                it.conditions.max()
            },
            {
                it.transition
            },
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntersectingMultiArcConditions

        if (conditions != other.conditions) return false
        if (transition != other.transition) return false

        return true
    }

    override fun hashCode(): Int {
        var result = conditions.hashCode()
        result = 31 * result + transition.hashCode()
        return result
    }
}