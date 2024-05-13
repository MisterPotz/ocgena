package ru.misterpotz.ocgena.simulation_v2.entities_selection

import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.RandomLeveledCombinationIterator
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.Shuffler
import ru.misterpotz.ocgena.simulation_v2.entities.InputArcWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.MultiArcCondition
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import java.util.*

fun List<InputArcWrapper>.makePreplaceTokenRequrements(): List<Int> {
    return map { it.consumptionSpec.tokenRequirement() }
}

fun List<InputArcWrapper>.mapToPreplaces(): List<PlaceWrapper> {
    return map { it.fromPlace }
}

class IndependentMultiConditionGroup(
    val conditions: SortedSet<MultiArcCondition>,
    val transition: TransitionWrapper,
) : Comparable<IndependentMultiConditionGroup> {
    val relatedInputArcs by lazy(LazyThreadSafetyMode.NONE) {
        conditions.flatMap { it.arcs.ref }.toSet()
    }

    fun conditionArcSortedByStrongestDescending(): List<InputArcWrapper> {
        val maxConditions = relatedInputArcs.maxOf { it.underConditions.size }

        relatedInputArcs.filter { it.underConditions.size == maxConditions }

        return relatedInputArcs.sortedWith(InputArcWrapper.ByConditionSizeByArcSpecByTransitionEntries)
    }

    data class Iteration(
        val placeToIndex: Map<PlaceWrapper, Int>,
        val randomLeveledCombinationIterator: RandomLeveledCombinationIterator
    )

    fun makeRandomCombinationIterator(
        tokenSlice: TokenSlice,
        shuffler: Shuffler
    ): Iteration {
        val sortedArcs = conditionArcSortedByStrongestDescending()
        val tokenRequirements = sortedArcs.makePreplaceTokenRequrements()
        val orderedPreplaces = sortedArcs.mapToPreplaces()

        val randomLeveledCombinationIterator = RandomLeveledCombinationIterator(
            orderedPreplaces.map { it.tokenRangeAt(tokenSlice) },
            tokenRequirements,
            shuffler,
            mode = RandomLeveledCombinationIterator.Mode.BFS,
        )
        return Iteration(
            orderedPreplaces.withIndex()
                .associateBy({ (_, placeWrapper) -> placeWrapper }, { (index, _) -> index }),
            randomLeveledCombinationIterator
        )
    }

    override fun compareTo(other: IndependentMultiConditionGroup): Int {
        return comparator.compare(this, other)
    }

    companion object {
        val comparator = compareBy<IndependentMultiConditionGroup>(
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

        other as IndependentMultiConditionGroup

        if (conditions != other.conditions) return false
        if (transition != other.transition) return false

        return true
    }

    override fun hashCode(): Int {
        var result = conditions.hashCode()
        result = 31 * result + transition.hashCode()
        return result
    }

    override fun toString(): String {
        return "$transition (syncs: ${conditions.map { it.syncTarget }.joinToString(",")}) [${relatedInputArcs.joinToString(",")}]"
    }
}