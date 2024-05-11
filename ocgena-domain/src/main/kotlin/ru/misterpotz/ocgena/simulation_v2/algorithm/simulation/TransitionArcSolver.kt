package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import java.util.SortedMap
import java.util.SortedSet

typealias ReferencingLayer = Int

class Node<T>(
    val id: Long,
    val layer: Int,
    val data: T
) {
    val referenceCounter: MutableMap<ReferencingLayer, Int> = mutableMapOf()
    val connections: SortedSet<Node<T>> = sortedSetOf()

    fun addReferenceFrom(referencingLayer: Int) {
        require(this.layer != referencingLayer) {
            "tried to add reference from the same layer"
        }
        referenceCounter[referencingLayer] = referenceCounter.getOrPut(referencingLayer) { 0 } + 1
    }

    fun removeReferenceFrom(referencingLayer: Int) {
        referenceCounter[referencingLayer] =
            (referenceCounter.getOrPut(referencingLayer) { 0 } - 1).coerceAtLeast(0)
    }

    fun removeConnection(child: Node<T>) {
        connections.remove(child)
        child.removeReferenceFrom(layer)
    }

    fun getChildIteratorFilterByLayers(filters: List<Int>): MutableIterator<Node<T>> {
        return NodeIterator(connections, filters)
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


    class NodeIterator<T>(
        sortedSet: SortedSet<Node<T>>,
        private val layerFilters: List<Int>
    ) : MutableIterator<Node<T>> {
        private val internalIterator = sortedSet.iterator()

        private var nextNodeToGiveOut: Node<T>? = null
        private var lastGivenOutVersion: Node<T>? = null

        override fun hasNext(): Boolean {
            checkVersionAndUpdateElement()
            return lastGivenOutVersion != null && nextNodeToGiveOut == null
        }

        private fun checkVersionAndUpdateElement() {
            if (nextNodeToGiveOut == lastGivenOutVersion) {
                var next: Node<T>? = null
                while (internalIterator.hasNext()) {
                    val candidate = internalIterator.next()

                    if (candidate != null && candidate.layer in layerFilters) {
                        next = candidate
                        break;
                    } else if (candidate == null) {
                        next = null
                        break;
                    }
                }
                nextNodeToGiveOut = next
            }
        }

        override fun next(): Node<T> {
            checkVersionAndUpdateElement()
            return nextNodeToGiveOut!!.also {
                lastGivenOutVersion = it
            }
        }

        override fun remove() {
            internalIterator.remove()
        }
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
}

class Layer<T>(val level: Int) {
    val nodes: SortedSet<Node<T>> = sortedSetOf()
}

data class Path<T>(val nodes: MutableList<Node<T>>) {
    fun push(node: Node<T>) {
        nodes.add(node)
    }

    fun pop(): Node<T> {
        return nodes.removeLast()
    }

    fun copyStack(): List<Node<T>> {
        return nodes.toList()
    }
}

class Graph<T> {
    private val layers: SortedMap<Int, Layer<T>> = sortedMapOf()

    fun addNode(level: Int, node: Node<T>) {
        val layer = layers.getOrPut(level) { Layer(level) }
        layer.nodes.add(node)
    }

    fun addConnection(fromNode: Node<T>, toNode: Node<T>) {
        if (layers.getValue(fromNode.layer).level < layers.getValue(toNode.layer).level) {
            fromNode.connections.add(toNode)
        } else {
            throw IllegalArgumentException("Cannot add connection to same or higher layer")
        }
    }

    // Add node and connection methods remain unchanged...

    fun iterateConnectedCombinations(vararg layersToInclude: Int): Iterator<List<Node<T>>> {
        val relevantLayers = layers.filterKeys { it in layersToInclude }.toSortedMap()
        return ConnectionsCombinationIterator(relevantLayers)
    }

    fun cleanGraph() {

    }

    class ConnectionsCombinationIterator<T>(
        private val layers: SortedMap<Int, Layer<T>>
    ) :
        MutableIterator<List<Node<T>>> {
        val layersFilter = layers.keys.toList()
        val iterator = constructDumbIterator()
        private var lastGaveOutValue: List<Node<T>>? = null


        private fun constructDumbIterator(): Iterator<List<Node<T>>> {
            return iterator {
                val path = Path<T>(mutableListOf())

                layers[layers.firstKey()]!!.nodes.iterator().forEach {
                    path.push(it)
                    recursiveChildIteration(it, path)
                    path.pop()
                }
            }
        }

        suspend fun <T> SequenceScope<List<Node<T>>>.recursiveChildIteration(node: Node<T>, path: Path<T>) {
            if (node.layer == layersFilter.last()) {
                // as we navigated to last required layer we can yield currently generated combination
                yield(path.copyStack())
                return
            }

            node.getChildIteratorFilterByLayers(layersFilter).forEach {
                path.push(it)
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
            }
        }

        override fun remove() {
            require(lastGaveOutValue != null) {
                "tried removing unexisting element"
            }

            // removing connections
            lastGaveOutValue!!.reduce { leftNode, rightNode ->
                leftNode.removeConnection(rightNode)
                rightNode
            }
        }
    }
}

class TransitionArcSolver(
    val transition: TransitionWrapper,
) {

    fun getSolutions(tokenSlice: TokenSlice) {
        // need to consider the conditions
        // filtering it is

        val allPlacesHaveEnoughTokensForCondition = transition.inputArcs.all { arc ->
            arc.consumptionSpec.complies(tokenSlice.amountAt(arc.fromPlace))
        }

        if (!allPlacesHaveEnoughTokensForCondition) {
            return
        }
        // проверили что токенов в принципе хватит, дальше че?
        // нужно начать с самого забористого условия
        // как определить самое забористое условие? где пересекаются несколько транзишенов


        val strongestArcsApplicableTokens =
            transition.inputArcConditions.associateBy(
                keySelector = { it.arcWithStrongestCondition },
                valueTransform = { inputCondition ->
                    inputCondition.arcWithStrongestCondition.selectApplicableTokens(tokenSlice)
                    inputCondition.arcWithStrongestCondition.currentSolutionSeachFilteredTokens!!
                }
            )

        val strongestArcsHaveEnoughSatisfactoryTokens = strongestArcsApplicableTokens.all { (arc, tokens) ->
            arc.consumptionSpec.complies(tokens.size)
        }

        if (!strongestArcsHaveEnoughSatisfactoryTokens) {
            return
        }

        val buffer = SimpleTokenSlice(tokenSlice.relatedPlaces.toMutableSet())
        strongestArcsApplicableTokens.forEach { (arc, tokens) ->
            {
                buffer.modifyTokensAt(arc.fromPlace) {
                    it.addAll(tokens)
                }
            }

            // теперь нужно найти для каждого группового условия подходящие по синхронизации варианты
            // надо как-то не очень прямолинейно число солюшенов искать, надо за это число принять другое, солюшенов
            // может быть гипер дохрена и больше и нормально это может не получиться посчитать


            val inputArc = transition.inputArcs.maxBy { it.syncTransitions.size }
            if (inputArc.syncTransitions.size == 0) {
                // do standard logic
            } else {
                // applicable tokens
                val tokens = tokenSlice.tokensAt(inputArc.fromPlace).filter { token ->
                    inputArc.syncTransitions.all { it in token.visitedTransitions }
                }
            }

            transition.inputArcConditions.map {

            }


            // need to start from the most narrow condition

            for ()
        }
    }

    class ArcGroupCondition(
        val transition: TransitionWrapper,
        val fromPlaces: Places,

        ) {
        fun isSatisfied(tokenSlice: TokenSlice) {

        }

        fun get
    }
