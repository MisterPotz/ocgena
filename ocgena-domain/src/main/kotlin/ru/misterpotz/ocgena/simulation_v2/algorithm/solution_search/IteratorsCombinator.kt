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