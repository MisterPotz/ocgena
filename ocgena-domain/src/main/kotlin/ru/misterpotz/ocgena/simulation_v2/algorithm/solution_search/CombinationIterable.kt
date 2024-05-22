package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

class CombinationIterable<T>(
    private val items: List<T>,
    private val combinationSize: Int,
) : Iterable<List<T>> {
    private val buffer = ArrayList<T>(combinationSize)

    private suspend fun SequenceScope<List<T>>.generateCombination(
        level: Int,
        startIndex: Int,
        current: MutableList<T>
    ) {
        if (level == combinationSize) {
            buffer.clear()
            buffer.addAll(current)
            yield(buffer)
            return
        }
        for (i in startIndex..items.lastIndex) {
            current[level] = items[i]
            generateCombination(level + 1, i + 1, current)
        }
    }

    private fun makeDumbIteraton() = iterator {
        generateCombination(0, 0, ArrayList(combinationSize))
    }

    override fun iterator(): Iterator<List<T>> {
        return object : Iterator<List<T>> {
            val iterator = makeDumbIteraton()

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): List<T> {
                return iterator.next()
            }
        }
    }
}