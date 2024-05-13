package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.utils.RandomIterator
import ru.misterpotz.ocgena.utils.Randomizer
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class Item<T>(
    val id: Long,
    val layer: Int,
    val data: T
) : Comparable<Item<T>> {

    val greenReferencesByLayer: MutableMap<Int, Int> = mutableMapOf()
    val redReferencesByLayer: MutableMap<Int, Int> = mutableMapOf()

    val redConnections: SortedSet<Item<T>> = sortedSetOf()
    val greenConnections: SortedSet<Item<T>> = sortedSetOf()

    fun MutableMap<Int, Int>.decrement(layer: Int) {
        put(layer, (getOrPut(layer) { 0 } - 1).coerceAtLeast(0))
    }

    fun MutableMap<Int, Int>.increment(layer: Int) {
        put(layer, (getOrPut(layer) { 0 } + 1))
    }

    private var markedAsDeleted = false

//    val invalidAssociatedPaths: MutableList<HashSet<CombinationNodeWrapper<T>>> = mutableListOf()

//    fun getReferenceCountToThisNode() = referenceCounter

    fun hasGreenReferences(): Boolean {
        return greenReferencesByLayer.isNotEmpty() && greenReferencesByLayer.values.reduce { acc, i -> acc + i } != 0
    }

    fun hasRedReferences(): Boolean {
        return redReferencesByLayer.isNotEmpty() && redReferencesByLayer.values.reduce { acc, i -> acc + i } != 0
    }

    fun addReferenceFrom(layer: Int, color: Color) {
        when (color) {
            Color.GREEN -> greenReferencesByLayer
            Color.RED -> redReferencesByLayer
        }.let {
            it.increment(layer)
        }
    }

    fun checkTotalRedReferenceAtNodeLayer(totalLayerNodes: Int): Boolean {
        return redReferencesByLayer.getOrPut(layer) { 0 } + redConnections.fold(0) { acc, item ->
            (acc + 1).takeIf { item.layer == layer } ?: acc
        } == totalLayerNodes
    }

    fun checkRedReferencedAtPriorityLayer(layer: Int, totalLayerNodes: Int): Boolean {
        require(this.layer > layer)
        return redReferencesByLayer.getOrPut(layer) { 0 } == totalLayerNodes
    }

    fun checkThisNodeViableForDeletion(graph: GreenRedLayeredGraph<T>): Boolean {
        return if (!checkTotalRedReferenceAtNodeLayer(graph.getLayer(layer).nodes.size)) {
            for (i in 0..<layer) {
                if (checkRedReferencedAtPriorityLayer(i, graph.getLayer(i).nodes.size)) {
                    prepareToDeletion()
                    return true
                }
            }
            false
        } else {
            prepareToDeletion()
            true
        }
    }

    fun isRedLeaf(): Boolean {
        return redConnections.size == 0
    }

    fun isGreenLeaf(): Boolean {
        return greenConnections.size == 0
    }

    fun directRedChildrenMarkedAsDeleted(): Boolean {
        return redConnections.all { it.markedAsDeleted }
    }

    fun directGreenChildrenMarkedAsDeleted(): Boolean {
        return greenConnections.all { it.markedAsDeleted }
    }

    fun getConnection(toItem: Item<T>): Color? {
        if (toItem in greenConnections) return Color.GREEN
        if (toItem in redConnections) return Color.RED
        return null
    }

    fun addChild(node: Item<T>, type: Color) {
        require(node.layer > layer)
        node.addReferenceFrom(layer, type)
        when (type) {
            Color.GREEN -> greenConnections.add(node)
            Color.RED -> redConnections.add(node)
        }
    }


    fun decrementReference(layer: Int, type: Color) {
        when (type) {
            Color.GREEN -> greenReferencesByLayer.decrement(layer)
            Color.RED -> redReferencesByLayer.decrement(layer)
        }
    }

    fun hasAnyParents(): Boolean {
        return hasRedReferences() || hasGreenReferences()
    }

    fun removeConnection(child: Item<T>, type: Color) {
        child.decrementReference(layer, type)
        when (type) {
            Color.GREEN -> greenConnections
            Color.RED -> redConnections
        }.remove(child)
    }

    fun releaseReferencesToChildren() {
        for (connection in greenConnections) {
            connection.decrementReference(layer, Color.GREEN)
        }
        for (connection in redConnections) {
            connection.decrementReference(layer, Color.RED)
        }
    }

    fun allConnections() = iterator {
        yieldAll(greenConnections)
        yieldAll(redConnections)
    }

    fun markAsDeleted() {
        markedAsDeleted = true
    }

    fun prepareToDeletion() {
        markAsDeleted()
        releaseReferencesToChildren()
    }

    fun isMarkedAsDeleted(): Boolean = markedAsDeleted

    fun removeAnyDeletedChildren() {
        redConnections.removeAll { it.markedAsDeleted }
        greenConnections.removeAll { it.markedAsDeleted }
    }

    override fun compareTo(other: Item<T>): Int {
        return id.compareTo(other.id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node<*>

        return id == other.id
    }


    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Node($data)"
    }
}


enum class Color {
    GREEN,
    RED
}

class GreenRedLayeredGraph<T>() {
    private val layers: SortedMap<Int, ItemLayer<T>> = sortedMapOf()

    private var nodeIdIssuer = 0L
    val STUB: Byte = 0
    val invalidAssociatedPaths: HashMap<Int, MutableList<HashSet<Item<T>>>> = hashMapOf()

    fun addInvalidSet(set: HashSet<Item<T>>) {
        invalidAssociatedPaths.getOrPut(set.hashCode()) {
            mutableListOf()
        }.add(set)
    }

    fun checkInvalidPath(path: HashSet<Item<T>>): Boolean {
        val hashCode = path.hashCode()

        return invalidAssociatedPaths.getOrPut(hashCode) {
            mutableListOf()
        }.contains(path)
    }

    fun getLayer(level: Int): ItemLayer<T> {
        return layers[level]!!
    }

    fun addNode(/*level: Int,*/ item: Item<T>) {
        val itemLayer = layers.getOrPut(item.layer) { ItemLayer(item.layer) }
        itemLayer.nodes.add(item)
    }

    fun addNode(layer: Int, data: T) {
        val node = Item(nodeIdIssuer++, layer, data)
        addNode(node)
    }

    fun printByLayer() {
        for ((key, layer) in layers) {
            println("key - ${layer.nodes}")
        }
    }

    fun addConnection(fromNode: Item<T>, toNode: Item<T>, color: Color) {
        if (layers.getValue(fromNode.layer).layer < layers.getValue(toNode.layer).layer) {
            fromNode.addChild(toNode, color)
        } else {
            throw IllegalArgumentException("Cannot add connection to same or higher layer")
        }
    }

    fun partialGraphClean() {
        for (layer in layers.values) {
            layer.deleteMarkedNodes()
        }
    }

    fun verifyGraphIntegrity() {
        require(layers.all { it.value.verifyLayerIntegrity(); true; })
    }


    interface ItemGraphParser<T> {
        fun next(randomizer: Randomizer): Item<T>?
        fun currentCombination(): List<List<Item<T>>>
        fun isCurrentCombinationSatisfactory(): Boolean
        fun invalidateCombination()
    }

    interface LayerIterator<T> {
        val layer: Int
        fun hasNext(): Boolean
        fun next(randomizer: Randomizer): Item<T>
    }

    class SimpleLayerIterator<T>(
        private val itemLayer: ItemLayer<T>,
        private val startItem: Item<T>
    ) : LayerIterator<T> {

        override val layer: Int
            get() = itemLayer.layer

        private var lastItem: Item<T> = startItem
        private var gaveOutStartItem = false

        private fun checkGaveOutStart(): Item<T>? {
            return if (!gaveOutStartItem) {
                startItem
            } else {
                null
            }
        }

        override fun hasNext(): Boolean {
            checkGaveOutStart()?.let {
                return true
            }
            val validSearchRange = validSearchRange

            return itemLayer.nodes.nodesAny(validSearchRange) { i: Int, item: Item<T> ->
                true
            }
        }

        private val validSearchRange
            get() = itemLayer.indexOf(lastItem)..itemLayer.nodes.indices.last

        override fun next(randomizer: Randomizer): Item<T> {
            checkGaveOutStart()?.let {
                gaveOutStartItem = true
                return it
            }

            return itemLayer.nodes.nodesRandomSelect(validSearchRange, randomizer) { i: Int, item: Item<T> ->
                true
            }?.also {
                lastItem = it
            } ?: throw IllegalStateException()
        }
    }

    class TransitiveAssociationsNodeIterator<T>(
        private val startingNode: Item<T>,
        private val tokensPerLayer: List<Int>,
        private val graph: GreenRedLayeredGraph<T>,
    ) : ItemGraphParser<T> {
        private val startingLayer = startingNode.layer
        private val buffer: HashSet<Item<T>> = hashSetOf()

        private fun MutableList<MutableList<Item<T>>>.updateLastValue(newValue : Item<T>) {
            lastOrNull()?.let {
                it.clear()
                it.add(newValue)
            }
        }

        private val combinationStackIterator = CombinationStackIterator(
            IteratorStack<T>().apply {
                push(
                    SimpleLayerIterator(
                        itemLayer = graph.getLayer(startingLayer),
                        startItem = startingNode,
                    )
                )
            },
            currentCombination = Combination(mutableListOf(mutableListOf(startingNode)))
        )

        private fun trySelectTokenOfLayer(randomizer: Randomizer, layerIndex: Int): Item<T>? {
            return if (layerIndex !in tokensPerLayer.indices) {
                null
            } else {
                val layer = graph.getLayer(layerIndex)

                combinationStackIterator.writeCombinationToBuffer(buffer)

                val randomIterator =
                    RandomIterator(layer.nodes.size, rangeToSelectFrom = layer.nodes.indices, randomizer)

                var localCandidate: Item<T>? = null
                while (randomIterator.hasNext() && localCandidate == null) {
                    val attemptToken = layer.nodes.get(randomIterator.next())
                    buffer.add(attemptToken)
                    if (!graph.checkInvalidPath(buffer)) {
                        localCandidate = attemptToken
                    }
                    buffer.remove(attemptToken)
                }
                buffer.clear()

                if (localCandidate != null) {
                    val layerIterator = SimpleLayerIterator(itemLayer = layer, localCandidate)
                    combinationStackIterator.push(layerIterator)
                    localCandidate
                } else {
                    null
                }
            }
        }

        // different randomizer won't change the result if null was returned previously
        override fun next(randomizer: Randomizer): Item<T>? {
            return if (combinationStackIterator.hasNext()) {
                combinationStackIterator.next(randomizer)
            } else {
                val lastNode = combinationStackIterator.getLast()
                if (lastNode != null) {
                    trySelectTokenOfLayer(randomizer, lastNode.layer + 1)
                } else {
                    null
                }
            }
        }

        override fun currentCombination(): List<List<Item<T>>> {
            return combinationStackIterator.getCurrentCombination()
        }

        override fun isCurrentCombinationSatisfactory(): Boolean {
            return combinationStackIterator.currentCombinationComplies(tokensPerLayer)
        }

        override fun invalidateCombination() {
            val hashset = hashSetOf<Item<T>>()
            combinationStackIterator.writeCombinationToBuffer(hashset)
            graph.addInvalidSet(hashset)
        }
    }

    class IteratorStack<T> {
        private val list: MutableList<LayerIterator<T>> = mutableListOf()

        val size
            get() = list.size

        fun isEmpty(): Boolean {
            return list.isEmpty()
        }

        fun push(item: LayerIterator<T>) {
            list.add(item)
        }

        fun pop() {
            list.removeLast()
        }

        fun peek(): LayerIterator<T> {
            return list.last()
        }
    }

    class CombinationStackIterator<T>(
        private val stack: IteratorStack<T> = IteratorStack(),
        private var currentCombination: Combination<T> = Combination(mutableListOf())
    ) {
        fun push(item: LayerIterator<T>) {
            stack.push(item)
            currentCombination.startNewLevel()
        }

        fun writeCombinationToBuffer(buffer: HashSet<Item<T>>) {
            currentCombination.writeToBuffer(buffer)
        }

        fun pop() {
            stack.pop()
            currentCombination.popLevel()
            require(stack.size == currentCombination.size)
        }

        fun isEmpty(): Boolean {
            return stack.isEmpty()
        }

        fun hasNext(): Boolean {
            return peek().hasNext()
        }

        fun next(randomizer: Randomizer): Item<T> {
            return peek().next(randomizer).also {
                currentCombination.appendItem(it)
            }
        }

        private fun peek(): LayerIterator<T> {
            return stack.peek()
        }

        fun currentCombinationComplies(tokensPerLevel: List<Int>): Boolean {
            return currentCombination.fullyCompliesWithRequirements(
                tokensPerLevel
            )
        }

        fun getCurrentCombination(): List<List<Item<T>>> {
            return currentCombination.items.toList()
        }

        fun getLast(): Item<T>? {
            return currentCombination.items.lastOrNull()?.lastOrNull()
        }

        fun isLastLevelElement(): Boolean {
            return currentCombination.isLastLevelElement()
        }

        fun getTwoLastNodes(): Pair<Item<T>, Item<T>>? {
            require(currentCombination.size > 0)

            val path = currentCombination.items

            val lastSegment = path.size.takeIf { it > 0 }?.let { size ->
                path[size - 1]
            }
            val preLastSegment = path.size.takeIf { it > 1 }?.let { size ->
                path[size - 2]
            }

            return when {
                lastSegment?.size == 1 && preLastSegment != null -> {
                    Pair(preLastSegment.last(), lastSegment.last())
                }

                lastSegment != null && lastSegment.size > 2 -> {
                    Pair(lastSegment[lastSegment.size - 2], lastSegment[lastSegment.size - 1])
                }

                else -> {
                    null
                }
            }
        }
    }

    data class Combination<T>(val items: MutableList<MutableList<Item<T>>>) {

        val size
            get() = items.size

        fun writeToBuffer(buffer: HashSet<Item<T>>) {
            buffer.clear()
            for (i in items) {
                for (level in i) {
                    buffer.add(level)
                }
            }
        }

        fun appendItem(itemWrapper: Item<T>) {
            require(items.size > 0)

            items.last().add(itemWrapper)
        }

        fun fullyCompliesWithRequirements(tokensPerLevel: List<Int>): Boolean {
            require(items.size <= tokensPerLevel.size)
            if (items.size != tokensPerLevel.size) return false

            items.forEachIndexed { index, itemWrappers ->
                if (itemWrappers.size != tokensPerLevel[index]) return false
            }

            return true
        }

        fun startNewLevel() {
            items.add(mutableListOf())
        }

        fun isLastLevelElement(): Boolean {
            return items.lastOrNull()?.size == 1
        }

        fun popLevel() {
            items.removeLastOrNull()
        }
    }

    class ItemLayer<T>(val layer: Int) {
        val nodes: SortedSet<Item<T>> = sortedSetOf()

        fun deleteMarkedNodes() {
            nodes.forEach { it.removeAnyDeletedChildren() }
            nodes.removeAll { it.isMarkedAsDeleted() }
            verifyLayerIntegrity()
        }

        fun verifyLayerIntegrity() {
            val traitorNode = nodes.find { it.isMarkedAsDeleted() }
            if (traitorNode != null) {
                throw IllegalStateException("during integrity no undeleted nodes should be left")
            }
        }

        fun indexOf(item: Item<T>): Int {
            return nodes.indexOf(item)
        }
    }
}

fun <T> SortedSet<Item<T>>.nodesAny(range: IntRange, predicate: (Int, Item<T>) -> Boolean): Boolean {
    forEachIndexed { index, item ->
        if (index in range && predicate(index, item)) {
            return true
        }
    }
    return false
}

private fun <T> SortedSet<Item<T>>.get(index: Int): Item<T> {
    forEachIndexed { i, item ->
        if (i == index) {
            return item
        };

    }
    throw IllegalStateException()
}

private fun <T> SortedSet<Item<T>>.nodesRandomSelect(
    searchRange: IntRange,
    randomizer: Randomizer,
    predicate: (Int, Item<T>) -> Boolean
): Item<T>? {
    var count = 0

    forEachIndexed { index, item ->
        if (index in searchRange && predicate(index, item)) {
            count++
        }
    }

    val randomIterator = RandomIterator(1, 0..<count, randomizer)

    val indexToSelect = randomIterator.next()

    count = 0

    forEachIndexed { index, item ->
        val correctItem = predicate(index, item)

        if (correctItem && count == indexToSelect) {
            return item
        } else if (correctItem) {
            count++
        }
    }

    return null
}