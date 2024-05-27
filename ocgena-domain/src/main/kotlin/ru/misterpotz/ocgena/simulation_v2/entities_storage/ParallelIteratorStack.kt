package ru.misterpotz.ocgena.simulation_v2.entities_storage

class ParallelIteratorStack<T : Comparable<T>>(
    iterables: List<Iterable<T>>,
) : Iterator<Iterator<Int>> {
    private val iterators = iterables.map { it.iterator() }
    private val lastResults: MutableList<T?> = MutableList(iterables.size) { null }
    private var lastMin: T? = null
    private var nextMin: T? = null
    private var iteratorsNotExhausted: Boolean = false

    inner class MinIndexIterator : Iterator<Int> {
        private var nextCandidateIndex: Int? = null
        private var exhausted: Boolean = false
        private var stoppedAt: Int = 0

        fun reset() {
            exhausted = lastMin == null
            stoppedAt = 0
            findNextCandidate()
        }

        private fun findNextCandidate() {
            nextCandidateIndex = null
            if (!exhausted) {
                for (i in stoppedAt until lastResults.size) {
                    if (lastResults[i] == lastMin) {
                        nextCandidateIndex = i
                        stoppedAt = i + 1
                        return
                    }
                }
                exhausted = true
            }
        }

        init {
            findNextCandidate()
        }

        override fun hasNext(): Boolean {
            return nextCandidateIndex != null
        }

        override fun next(): Int {
            if (!hasNext()) throw NoSuchElementException()
            return nextCandidateIndex!!.also {
                findNextCandidate()
            }
        }
    }

    private val minIterator = MinIndexIterator()

    fun iteratorOfIndicesWithMin(): Iterator<Int> {
        return minIterator.also { it.reset() }
    }

    private fun findNextMin(): T? {
        val currentLastMin = lastMin ?: return null
        var nextMinCandidate: T? = null
        for (i in lastResults) {
            if (i != null && i > currentLastMin) {
                if (nextMinCandidate == null || i < nextMinCandidate) {
                    nextMinCandidate = i
                }
            }
        }
        return nextMinCandidate
    }

    fun getCurrentItemAt(index: Int): T? {
        if (index !in iterators.indices) throw IndexOutOfBoundsException()
        return lastResults[index]
    }

    fun allMoveNext() {
        var min: T? = null
        var iteratorsNotExhausted = false
        for (i in iterators.indices) {
            if (iterators[i].hasNext()) {
                lastResults[i] = iterators[i].next()
                if (iterators[i].hasNext()) {
                    iteratorsNotExhausted = true
                }
                if (min == null || lastResults[i]!! < min) {
                    min = lastResults[i]
                }
            } else {
                lastResults[i] = null
            }
        }
        lastMin = min
        this.iteratorsNotExhausted = iteratorsNotExhausted
        updateNextCandidate()
    }

    private fun updateNextCandidate() {
        this.nextMin = findNextMin()
    }

    override fun hasNext(): Boolean {
        return nextMin != null || iteratorsNotExhausted
    }

    override fun next(): Iterator<Int> {
        if (!hasNext()) throw NoSuchElementException()

        if (minIterator.hasNext()) return minIterator
        else if (nextMin != null) {
            this.lastMin = nextMin
            this.nextMin = null
            updateNextCandidate()
        } else if (iteratorsNotExhausted) {
            allMoveNext()
        } else {
            throw IllegalStateException()
        }
        return iteratorOfIndicesWithMin()
    }

    init {
        allMoveNext() // Initialize the iterators and find the first minimum
    }
}

class ParallelIteratorStackChat<T : Comparable<T>>(
    private val iterables: List<Iterable<T>>,
) : Iterator<Iterator<Int>> {

    private val iterators = iterables.map { it.iterator() }
    private val currentValues = MutableList<T?>(iterables.size) { null }
    private var isInitialized = false

    init {
        initialize()
    }

    private fun initialize() {
        for (i in iterators.indices) {
            if (iterators[i].hasNext()) {
                currentValues[i] = iterators[i].next()
            }
        }
        isInitialized = true
    }

    private fun findCurrentMin(): T? {
        var min: T? = null
        for (value in currentValues) {
            if (value != null) {
                if (min == null || value < min) {
                    min = value
                }
            }
        }
        return min
    }

    override fun hasNext(): Boolean {
        if (!isInitialized) {
            initialize()
        }
        return currentValues.any { it != null }
    }

    override fun next(): Iterator<Int> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        val currentMin = findCurrentMin() ?: throw IllegalStateException("Current minimum not found")

        val indicesWithMin = mutableListOf<Int>()
        for (i in currentValues.indices) {
            if (currentValues[i] == currentMin) {
                indicesWithMin.add(i)
                if (iterators[i].hasNext()) {
                    currentValues[i] = iterators[i].next()
                } else {
                    currentValues[i] = null
                }
            }
        }

        return indicesWithMin.iterator()
    }
}

class ParallelIteratorStackChatV2<T : Comparable<T>>(
    iterables: List<Iterable<T>>,
) : Iterator<Iterator<Int>> {

    private val iterators = iterables.map { it.iterator() }
    private val currentValues = MutableList<T?>(iterables.size) { null }
    private var isInitialized = false
    private val indicesOffset = MutableList(iterables.size) { 0 }
    private val minIndicesBuffer = mutableListOf<Int>()

    init {
        var offset = 0
        for ((index, iterable) in iterables.withIndex()) {
            indicesOffset[index] = (offset)
            offset += iterable.count()
        }
        initialize()
    }

    private fun initialize() {
        for (i in iterators.indices) {
            if (iterators[i].hasNext()) {
                currentValues[i] = iterators[i].next()
            }
        }
        isInitialized = true
    }

    private fun findCurrentMin(): T? {
        var min: T? = null
        for (value in currentValues) {
            if (value != null) {
                if (min == null || value < min) {
                    min = value
                }
            }
        }
        return min
    }

    override fun hasNext(): Boolean {
        if (!isInitialized) {
            initialize()
        }
        return currentValues.any { it != null }
    }

    override fun next(): Iterator<Int> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        val currentMin = findCurrentMin() ?: throw IllegalStateException("Current minimum not found")

        minIndicesBuffer.clear()
        for (i in currentValues.indices) {
            if (currentValues[i] == currentMin) {
                minIndicesBuffer.add(indicesOffset[i])
                if (iterators[i].hasNext()) {
                    currentValues[i] = iterators[i].next()
                    indicesOffset[i]++
                } else {
                    currentValues[i] = null
                }
            }
        }

        return minIndicesBuffer.iterator()
    }
}

data class HistoricalIndices<T>(
    val historyEntry: T,
    val iterator: Iterator<Int>,
)

class ParallelIteratorStackChatV3<T : Comparable<T>>(
    iterables: List<Iterable<T>>,
    private val exclusion: Set<T> = emptySet(),
) : Iterator<HistoricalIndices<T>> {

    private val iterators = iterables.map { it.iterator() }
    private val currentValues = MutableList<T?>(iterables.size) { null }
    private var isInitialized = false
    private val minIndicesBuffer = mutableListOf<Int>()

    init {
        initialize()
    }

    private fun initialize() {
        for (i in iterators.indices) {
            findNotExcludedForIndex(i)
        }
        isInitialized = true
    }

    private fun findCurrentMin(): T? {
        var min: T? = null
        for (value in currentValues) {
            if (value != null) {
                if (min == null || value < min) {
                    min = value
                }
            }
        }
        return min
    }

    private fun findNotExcludedForIndex(index: Int) {
        currentValues[index] = null
        while (iterators[index].hasNext()) {
            val nextValue = iterators[index].next()
            if (nextValue !in exclusion) {
                currentValues[index] = nextValue
                break
            }
        }
    }

    override fun hasNext(): Boolean {
        if (!isInitialized) {
            initialize()
        }
        return currentValues.any { it != null }
    }

    override fun next(): HistoricalIndices<T> {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        val currentMin = findCurrentMin() ?: throw IllegalStateException("Current minimum not found")

        minIndicesBuffer.clear()
        for (i in currentValues.indices) {
            if (currentValues[i] == currentMin) {
                minIndicesBuffer.add(i)
                findNotExcludedForIndex(i)
            }
        }
        return HistoricalIndices(currentMin, minIndicesBuffer.iterator())
    }
}