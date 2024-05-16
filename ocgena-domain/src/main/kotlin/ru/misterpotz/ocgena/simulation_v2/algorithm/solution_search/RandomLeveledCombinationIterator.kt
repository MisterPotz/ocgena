package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import kotlin.random.Random

class CombinationIterable(
    private val indicesToVisit: List<Int>,
    private val combinationSize: Int,
) : MutableIterable<List<Int>> {
    private val discardedCombinations: MutableSet<Set<Int>> = mutableSetOf()

    private var lastCombination: List<Int>? = null

    private suspend fun SequenceScope<List<Int>>.generateCombination(
        level: Int,
        startIndex: Int,
        current: MutableList<Int>
    ) {
        if (level == combinationSize) {
            yield(current.toList())
            return
        }
        for (i in startIndex..indicesToVisit.lastIndex) {
            current[level] = indicesToVisit[i]
            generateCombination(level + 1, i + 1, current)
        }
    }

    private fun makeDumbIteraton() = iterator {
        generateCombination(0, 0, MutableList(combinationSize) { 0 })
    }

    override fun iterator(): MutableIterator<List<Int>> {
        return object : MutableIterator<List<Int>> {
            val iterator = makeDumbIteraton()

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): List<Int> {
                return iterator.next()
            }

            override fun remove() {
                lastCombination?.let { lastCombination ->
                    discardedCombinations.add(lastCombination.toSet())
                }
            }
        }
    }
}

interface Shuffler {
    // makes completely shuffled indices
    fun makeShuffled(intRange: IntRange): List<Int>
    // select random from the given list
    fun select(ints: List<Int>) : Int
    fun select(longs: LongRange) : Long
    fun select(indices : IntRange) : Int
}

class NormalShuffler(val random: Random) : Shuffler {
    override fun makeShuffled(intRange: IntRange): List<Int> {
        return intRange.shuffled(random)
    }

    override fun select(ints: List<Int>): Int {
        return ints.random(random)
    }

    override fun select(longs: LongRange): Long {
        return longs.random(random)
    }

    override fun select(indices: IntRange): Int {
        return indices.random(random)
    }
}

class RandomLeveledCombinationIterator(
    ranges: List<IntRange>,
    private val nodesAtLevel: List<Int>,
    private val shuffler: Shuffler,
    private val mode: Mode,
) :
    Iterator<List<List<Int>>> {
    enum class Mode { BFS, DFS }

    private val combinationFuel = mutableMapOf<Int, List<Int>>()
    private val combinationSize = ranges.size

    init {
        val initIterators = ranges.map { shuffler.makeShuffled(it) }

        combinationFuel.putAll(
            initIterators.withIndex().associateBy({ it.index }) { it.value })

    }

    private val levelIterables = List(ranges.size) { index ->
        CombinationIterable(combinationFuel[index]!!, nodesAtLevel[index])
    }

    private val dumbIterator = iterator {
        val reorderedLevels = when (mode) {
            Mode.BFS -> levelIterables.reversed()
            Mode.DFS -> levelIterables
        }
        generateCombinationsDepthFirst(
            0,
            reorderedLevels,
            MutableList(combinationSize) { listOf() }
        )
    }

    private suspend fun SequenceScope<List<List<Int>>>.generateCombinationsDepthFirst(
        level: Int,
        levelIterables: List<CombinationIterable>,
        current: MutableList<List<Int>>
    ) {
        if (level == combinationSize) {
            val reorderedCombination = when (mode) {
                Mode.BFS -> current.reversed()
                Mode.DFS -> current.toList()
            }
            yield(reorderedCombination)
            return
        }
        val thisLevelIterable = levelIterables[level].iterator()

        for (i in thisLevelIterable) {
            current[level] = i
            generateCombinationsDepthFirst(level + 1, levelIterables, current)
        }
    }

    override fun hasNext(): Boolean {
        return dumbIterator.hasNext()
    }

    override fun next(): List<List<Int>> {
        return dumbIterator.next()
    }
}