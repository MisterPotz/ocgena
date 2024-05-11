package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import java.util.*
import kotlin.collections.HashSet

typealias ReferencingLayer = Int

class CombinationNodeWrapper<T>(val node: Node<T>, val parent: Node<T>?) {
    override fun toString(): String {
        return "C_$node"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CombinationNodeWrapper<*>

        return node == other.node
    }

    override fun hashCode(): Int {
        return node.hashCode()
    }
}

class Node<T>(
    val id: Long,
    val layer: Int,
    val data: T
) : Comparable<Node<T>> {
    private val referenceCounter: MutableMap<ReferencingLayer, Int> = mutableMapOf()
    val connections: SortedSet<Node<T>> = sortedSetOf()

    private var markedAsDeleted = false

    val invalidAssociatedPaths: MutableList<HashSet<CombinationNodeWrapper<T>>> = mutableListOf()

    fun getReferenceCountToThisNode() =
        if (referenceCounter.isEmpty()) 0 else referenceCounter.values.reduce { acc, i -> acc + i }

    fun hasParents(): Boolean {
        return getReferenceCountToThisNode() > 0
    }

    fun getReferenceCountFromLayer(layer: Int) = referenceCounter.getOrElse(layer) { 0 }

    fun addReferenceFrom(referencingLayer: Int) {
        require(this.layer != referencingLayer) {
            "tried to add reference from the same layer"
        }
        referenceCounter[referencingLayer] = referenceCounter.getOrPut(referencingLayer) { 0 } + 1
    }

    fun isLeaf(): Boolean {
        return connections.size == 0
    }

    fun directChildrenMarkedAsDeleted(): Boolean {
        return connections.all { it.markedAsDeleted }
    }

    fun addChild(node: Node<T>) {
        require(node.layer > layer)
        node.addReferenceFrom(layer)
        connections.add(node)
    }

    fun removeReferenceFrom(referencingLayer: Int) {
        referenceCounter[referencingLayer] =
            (referenceCounter.getOrPut(referencingLayer) { 0 } - 1).coerceAtLeast(0)
    }

    fun removeConnection(child: Node<T>) {
        connections.remove(child)
        child.removeReferenceFrom(layer)
    }

    fun releaseReferencesToChildren() {
        for (connection in connections) {
            connection.removeReferenceFrom(layer)
        }
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
        connections.removeAll { it.isMarkedAsDeleted() }
    }

    fun addInvalidAssociatedPath(set: HashSet<CombinationNodeWrapper<T>>) {
        invalidAssociatedPaths.add(set)
    }

    fun getChildIteratorFilterByLayers(
        filters: List<Int>,
        parentNode: Node<T>? = null
    ): MutableIterator<CombinationNodeWrapper<T>> {
        return TransitiveAssociationsNodeIterator(parentNode, connections, filters)
    }

    fun isConnectedToAllOtherLayers(totalLayersNumber: Int): Boolean {
        // has connection from each upper layer
        val hasConnectionsFromAllUppers = (0..<layer).all { it in referenceCounter.keys }

        return hasConnectionsFromAllUppers && run {
            val visitedLayerMask = MutableList(totalLayersNumber) { it <= layer }
            for (connection in connections) {
                visitedLayerMask[connection.layer] = true
            }
            ((layer + 1)..<totalLayersNumber).all { visitedLayerMask[it] }
        }
    }

    class IteratorStack<T>() {
        private val list: MutableList<MutableIterator<CombinationNodeWrapper<T>>> = mutableListOf()

        fun isEmpty(): Boolean {
            return list.isEmpty()
        }

        fun push(item: MutableIterator<CombinationNodeWrapper<T>>) {
            list.add(item)
        }

        fun pop() {
            list.removeLast()
        }

        fun peek(): MutableIterator<CombinationNodeWrapper<T>> {
            return list.last()
        }
    }

    class TransitiveAssociationsNodeIterator<T>(
        val parentNode: Node<T>?,
        sortedSet: SortedSet<Node<T>>,
        private val layerFilters: List<Int>
    ) : MutableIterator<CombinationNodeWrapper<T>> {
        private val internalIterator = object : MutableIterator<CombinationNodeWrapper<T>> {
            val iterator = sortedSet.iterator()
            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): CombinationNodeWrapper<T> {
                return CombinationNodeWrapper(iterator.next(), parentNode)
            }

            override fun remove() {
                iterator.remove()
            }
        }


        private var valueVersion: Int = 0
        private var nextNodeToGiveOut: CombinationNodeWrapper<T>? = null
            set(value) {
                valueVersion++
                field = value
            }
        private var givenValueVersion: Int = 0

        private val iteratorStack: IteratorStack<T> = IteratorStack<T>().apply {
            push(internalIterator)
        }

        override fun hasNext(): Boolean {
            checkVersionAndUpdateElement()
            return nextNodeToGiveOut != null
        }

        private fun checkVersionAndUpdateElement() {
            if (valueVersion == givenValueVersion) {
                var next: CombinationNodeWrapper<T>? = null

                while (iteratorStack.isEmpty().not()) {
                    val candidate = if (iteratorStack.peek().hasNext()) {
                        iteratorStack.peek().next()
                    } else {
                        iteratorStack.pop()
                        null
                    }
                    if (candidate == null) continue

                    if (candidate.node.layer in layerFilters) {
                        next = candidate
                        break;
                    } else if (candidate.node.layer <= layerFilters.last()) {
                        iteratorStack.push(candidate.node.getChildIteratorFilterByLayers(layerFilters))
                    }
                }
                nextNodeToGiveOut = next
            }
        }

        override fun next(): CombinationNodeWrapper<T> {
            checkVersionAndUpdateElement()
            return nextNodeToGiveOut!!.also {
                givenValueVersion = valueVersion
            }
        }

        override fun remove() {
            internalIterator.remove()
        }
    }

    override fun compareTo(other: Node<T>): Int {
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

class Layer<T>(val level: Int) {
    val nodes: SortedSet<Node<T>> = sortedSetOf()

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

    fun findAndMarkNodesToDelete(totalLayers: Int) {
        for (lastLayer in 0..<totalLayers) {

        }
        for (node in nodes) {
            if (!node.isConnectedToAllOtherLayers(totalLayers)) {
                node.prepareToDeletion()
            }
        }
    }
}

data class Path<T>(val nodes: MutableList<CombinationNodeWrapper<T>>) {
    fun push(node: CombinationNodeWrapper<T>) {
        nodes.add(node)
    }

    fun pop(): CombinationNodeWrapper<T> {
        return nodes.removeLast()
    }

    fun copyStack(): List<CombinationNodeWrapper<T>> {
        return nodes.toList()
    }
}

class LayeredGraph<T> {
    private val layers: SortedMap<Int, Layer<T>> = sortedMapOf()

    var nodeIdIssuer = 0L

    fun getLayer(level: Int): Layer<T> {
        return layers[level]!!
    }

    fun addNode(/*level: Int,*/ node: Node<T>) {
        val layer = layers.getOrPut(node.layer) { Layer(node.layer) }
        layer.nodes.add(node)
    }

    fun printByLayer() {
        for ((key, layer) in layers) {
            println("key - ${layer.nodes}")
        }
    }

    fun addConnection(fromNode: Node<T>, toNode: Node<T>) {
        if (layers.getValue(fromNode.layer).level < layers.getValue(toNode.layer).level) {
            fromNode.addChild(toNode)
        } else {
            throw IllegalArgumentException("Cannot add connection to same or higher layer")
        }
    }

    // Add node and connection methods remain unchanged...

    fun iterateConnectedCombinations(vararg layersToInclude: Int): MutableIterator<List<Node<T>>> {
        val relevantLayers = layers.filterKeys { it in layersToInclude }.toSortedMap()
        return ConnectionsCombinationIterator(relevantLayers)
    }

    fun pruneClean() {
        val totalLayers = layers.keys.size

        for (layer in 1..<totalLayers) {
            for (node in layers[layer]!!.nodes) {
                if (node.hasParents().not()) {
                    node.prepareToDeletion()
                }
            }
        }

        for (layer in (totalLayers - 3).downTo(0)) {
            for (node in layers[layer]!!.nodes) {
                for (connection in node.connections) {
                    if (connection.layer == totalLayers - 1 && connection.getReferenceCountToThisNode() == 1) {
                        connection.prepareToDeletion()
                    }
                }
            }
        }

        for (lastLayer in (totalLayers - 2) downTo 0) {
            val iterator = iterateConnectedCombinations(*(0..lastLayer).toList().toIntArray())
            iterator.forEach { combination ->
                val lastItem = combination.last()

                if (lastItem.isLeaf() || lastItem.directChildrenMarkedAsDeleted()) {
                    lastItem.prepareToDeletion()
                }
            }
        }

        partialGraphClean()
    }

    fun partialGraphClean() {
        for (layer in layers.values) {
            layer.deleteMarkedNodes()
        }
    }

    fun verifyGraphIntegrity() {
        require(layers.all { it.value.verifyLayerIntegrity(); true; })
    }

    class ConnectionsCombinationIterator<T>(
        private val layers: SortedMap<Int, Layer<T>>
    ) :
        MutableIterator<List<Node<T>>> {
        private val layersFilter = layers.keys.toList()
        val iterator = constructDumbIterator()
        private var lastGaveOutValue: List<CombinationNodeWrapper<T>>? = null


        private fun constructDumbIterator(): Iterator<List<CombinationNodeWrapper<T>>> {
            return iterator {
                val path = Path<T>(mutableListOf())

                layers[layers.firstKey()]!!.nodes.iterator().forEach {
                    path.push(CombinationNodeWrapper(it, null))
                    recursiveChildIteration(CombinationNodeWrapper(it, null), path)
                    path.pop()
                }
            }
        }

        private fun <T> checkPathIsValid(path: MutableList<CombinationNodeWrapper<T>>): Boolean {
            val pathToCheck = path.toHashSet()
            return path.none {
                it.node.invalidAssociatedPaths.any { invalidPath ->
                    invalidPath.intersect(pathToCheck).size == invalidPath.size
                }
            }
        }

        suspend fun <T> SequenceScope<List<CombinationNodeWrapper<T>>>.recursiveChildIteration(
            nodeWrapper: CombinationNodeWrapper<T>,
            path: Path<T>
        ) {
            println("entering recursive with $nodeWrapper")
            if (nodeWrapper.node.layer >= layersFilter.last() || path.nodes.size >= layersFilter.size) {
                println("checking $path")
                if (path.nodes.size == layersFilter.size && checkPathIsValid(path.nodes)) {
                    // as we navigated to last required layer we can yield currently generated combination
                    yield(path.copyStack())
                }
                return
            }

            nodeWrapper.node.getChildIteratorFilterByLayers(layersFilter, nodeWrapper.parent).forEach {
                path.push(it)
                println("pushing onto stack $it")
                recursiveChildIteration(it, path)
                path.pop()
            }
        }

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        override fun next(): List<Node<T>> {
            return iterator.next().also {
                lastGaveOutValue = it
            }.map { it.node }
        }

        /**
         * if size of combination is one, marks the entire node to be deleted
         * if size is bigger, then severs the connection
         */
        override fun remove() {
            require(lastGaveOutValue != null) {
                "tried removing unexisting element"
            }
            val lastGaveOutValue = lastGaveOutValue!!

            when {
                lastGaveOutValue.size == 1 -> {
                    lastGaveOutValue.last().parent?.removeConnection(lastGaveOutValue.last().node)
                    lastGaveOutValue.last().node.prepareToDeletion()
                }

                lastGaveOutValue.size == 2 &&
                        lastGaveOutValue.first().node == lastGaveOutValue.last().parent -> {
                    lastGaveOutValue.last().parent?.removeConnection(lastGaveOutValue.last().node)
                }

                else -> {
                    val invalidPathAssociation = lastGaveOutValue.map { it }.toHashSet()

                    for (node in lastGaveOutValue) {
                        node.node.addInvalidAssociatedPath(invalidPathAssociation)
                    }
                }
            }
        }
    }
}