package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

class IteratorsCombinator<T>(
    private val iterables: List<Iterable<T>>
) : Iterable<List<T>> {
    private suspend fun SequenceScope<List<T>>.generateCombinationsDepthFirst(
        level: Int, levelIterables: List<Iterable<T>>, current: MutableList<T?>
    ) {
        if (level == iterables.size) {
            yield(current.mapNotNull { it })
            return
        }
        val thisLevelIterable = levelIterables[level].iterator()

        for (i in thisLevelIterable) {
            current[level] = i
            generateCombinationsDepthFirst(level + 1, levelIterables, current)
        }
    }

    override fun iterator(): Iterator<List<T>> {
        val dumbIterator = iterator {
            generateCombinationsDepthFirst(0, iterables, MutableList(iterables.size) { null })
        }
        return object : Iterator<List<T>> {
            override fun hasNext(): Boolean {
                return dumbIterator.hasNext()
            }

            override fun next(): List<T> {
                return dumbIterator.next()
            }
        }
    }
}

class ControlledIteratorsCombinator<T>(
    private val iterables: List<Iterable<T>>
) : Iterable<List<T>> {

    private val iterators = mutableListOf<Iterator<T>>()
    private val currentElements = mutableListOf<T?>()
    private var isExhausted = false

    init {
        // Initialize iterators and currentElements
        for (iterable in iterables) {
            val iterator = iterable.iterator()
            iterators.add(iterator)
            if (iterator.hasNext()) {
                currentElements.add(iterator.next())
            } else {
                isExhausted = true
                break
            }
        }
    }

    fun advanceIndex(level: Int) {
        if (isExhausted || level !in iterators.indices) return

        // Advance the iterator at the specified level and reset subsequent levels
        if (iterators[level].hasNext()) {
            currentElements[level] = iterators[level].next()
        } else {
            resetIterator(level)
            if (level > 0) {
                advanceIndex(level - 1)
            } else {
                isExhausted = true
            }
        }

        // Reset subsequent levels
        for (i in level + 1 until iterators.size) {
            resetIterator(i)
        }
    }

    private fun resetIterator(level: Int) {
        iterators[level] = iterables[level].iterator()
        if (iterators[level].hasNext()) {
            currentElements[level] = iterators[level].next()
        } else {
            isExhausted = true
        }
    }

    override fun iterator(): Iterator<List<T>> {
        return object : Iterator<List<T>> {
            override fun hasNext(): Boolean {
                return !isExhausted
            }

            override fun next(): List<T> {
                if (!hasNext()) {
                    throw NoSuchElementException("No more elements")
                }

                val combination = currentElements.map { it!! }

                advanceIndex(iterators.size - 1)

                return combination
            }
        }
    }
}

fun main() {
    val iterables = listOf(
        listOf(1, 2, 3).asIterable(),
        listOf("a", "b").asIterable(),
        listOf(false, true).asIterable(), // Non-empty iterable
        listOf(4.5, 5.5).asIterable()
    )

    val combinator = ControlledIteratorsCombinator(iterables)

    val iterator = combinator.iterator()

    while (iterator.hasNext()) {
        val combination = iterator.next()
        println(combination)

        // Example of manually advancing an index
        if (combination[0] == 2 && combination[1] == "b") {
            combinator.advanceIndex(2)
        }
    }
}