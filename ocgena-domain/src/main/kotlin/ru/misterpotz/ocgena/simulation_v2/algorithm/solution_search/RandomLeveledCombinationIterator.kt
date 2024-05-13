package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.utils.RandomIterator
import ru.misterpotz.ocgena.utils.Randomizer

class CombinationIterator(
    private val indicesToVisit: List<Int>,
    private val combinationSize: Int,
) : MutableIterator<List<Int>> {
    private val discardedCombinations: MutableSet<Set<Int>> = mutableSetOf()

    private fun makeIterators() = (0..<combinationSize).map { indicesToVisit.iterator() }

    fun discardCombination(combination: Set<Int>) {
        discardedCombinations.add(combination)
    }

    private var lastCombination: List<Int>? = null

    private suspend fun SequenceScope<List<Int>>.generateCombination(
        level: Int,
        iterators: List<Iterator<Int>>,
        current: MutableList<Int>
    ) {
        if (level == combinationSize) {
            yield(current)
            return
        }
        current[level] = iterators[level].next()

        for (i in indicesToVisit.indices) {
            generateCombination(level + 1, iterators, current)
        }
    }

    private val dumbIterator = iterator {
        generateCombination(0, makeIterators(), MutableList(combinationSize) { 0 })
    }

    override fun hasNext(): Boolean {
        return dumbIterator.hasNext()
    }

    override fun next(): List<Int> {
        return dumbIterator.next()
    }

    override fun remove() {
        lastCombination?.let { lastCombination ->
            discardedCombinations.add(lastCombination.toSet())
        }
    }
}

class RandomLeveledCombinationIterator(
    private val ranges: List<IntRange>,
    private val nodesAtLevel: List<Int>,
    private val randomizer: Randomizer,
    private val mode : Mode,
) :
    Iterator<List<List<Int>>> {
    enum class Mode { BFS, DFS }
    private val combinationEntries = mutableMapOf<Int, List<Int>>()
    private val combinationSize = nodesAtLevel.size

    init {
        val initIterators = ranges.map { RandomIterator(it.count(), it, randomizer) }
        combinationEntries.putAll(
            initIterators.withIndex().associateBy({ it.index }) { it.value.asSequence().toList() })

    }

    private val levelIterators = ranges.mapIndexed { index, intRange ->
        CombinationIterator(combinationEntries[index]!!, nodesAtLevel[index])
    }

    private val discardCombinations: MutableSet<Set<Int>> = mutableSetOf()

    private val dumbIterator = iterator {
        val reorderedLevels = when (mode) {
            Mode.BFS -> levelIterators.reversed()
            Mode.DFS -> levelIterators
        }
        generateCombinationsDepthFirst(
            0,
            reorderedLevels,
            MutableList(combinationSize) { listOf() }
        )
    }

    private suspend fun SequenceScope<List<List<Int>>>.generateCombinationsDepthFirst(
        level: Int,
        levelIterators: List<CombinationIterator>,
        current: MutableList<List<Int>>
    ) {
        if (level == combinationSize) {
            val reorderedCombination = when (mode) {
                Mode.BFS -> current.reversed()
                Mode.DFS -> current
            }
            yield(reorderedCombination)
            return
        }

        while (levelIterators[level].hasNext()) {
            current[level] = levelIterators[level].next()
            generateCombinationsDepthFirst(level + 1, levelIterators, current)
        }
    }

    override fun hasNext(): Boolean {
        return dumbIterator.hasNext()
    }

    override fun next(): List<List<Int>> {
        return dumbIterator.next()
    }

    fun discardCurrentCombinationAtLevel(level : Int) {
        levelIterators[level].remove()
    }

    fun discardCombinationAtLevel(level: Int, set: Set<Int>) {
        levelIterators[level].discardCombination(set)
    }

    fun discardCombination(set: Set<Int>) {
        discardCombinations.add(set)
    }
}