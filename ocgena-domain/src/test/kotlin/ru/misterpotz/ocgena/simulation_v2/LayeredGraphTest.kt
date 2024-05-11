package ru.misterpotz.ocgena.simulation_v2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.LayeredGraph
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.Node

class LayeredGraphTest {

    fun List<Node<Int>>.by(data: Int): Node<Int> = find { it.data == data }!!

    fun createNodes() = listOf(
        Node(0, 0, 1),
        Node(1, 0, 5),

        Node(2, 1, 2),
        Node(3, 1, 4),
        Node(4, 1, 7),

        Node(5, 2, 3),
        Node(6, 2, 6),
        Node(7, 2, 9),

        Node(8, 3, 8),
    )

    fun initializeGraph(nodes: List<Node<Int>>): LayeredGraph<Int> {
        val graph = LayeredGraph<Int>()
        graph.apply {
            nodes.forEach {
                addNode(it)
            }

            addConnection(nodes.by(1), nodes.by(2))

            addConnection(nodes.by(5), nodes.by(2))
            addConnection(nodes.by(5), nodes.by(4))
            addConnection(nodes.by(5), nodes.by(6))

            addConnection(nodes.by(2), nodes.by(3))
            addConnection(nodes.by(2), nodes.by(8))
            addConnection(nodes.by(4), nodes.by(6))

            addConnection(nodes.by(7), nodes.by(8))
        }
        return graph
    }

    @Test
    fun `2LayeriteratorWorksAsExpected`() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)
        val iterator = graph.iterateConnectedCombinations(0, 2)

        Assertions.assertTrue(iterator.hasNext())

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(1), nodes.by(3)),
                listOf(nodes.by(5), nodes.by(3)),
                listOf(nodes.by(5), nodes.by(6)),
                listOf(nodes.by(5), nodes.by(6)),
            ),
            iterator.asSequence().toList()
        )
    }

    @Test
    fun `3LayeriteratorWorksAsExpected`() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)
        val iterator = graph.iterateConnectedCombinations(0, 1, 3)

        Assertions.assertTrue(iterator.hasNext())

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(1), nodes.by(2), nodes.by(8)),
                listOf(nodes.by(5), nodes.by(2), nodes.by(8)),
            ),
            iterator.asSequence().toList()
        )
    }

    @Test
    fun `2 layers combinatorics, starts not at 0 LayeriteratorWorksAsExpected`() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)
        val iterator = graph.iterateConnectedCombinations(1, 3)

        Assertions.assertTrue(iterator.hasNext())

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(2), nodes.by(8)),
                listOf(nodes.by(7), nodes.by(8)),
            ),
            iterator.asSequence().toList()
        )
    }

    @Test
    fun `1 layer iteration`() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)
        val iterator = graph.iterateConnectedCombinations(2)

        Assertions.assertTrue(iterator.hasNext())

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(3)),
                listOf(nodes.by(6)),
                listOf(nodes.by(9)),
            ),
            iterator.asSequence().toList()
        )
    }

    @Test
    fun `removing during iteration for case of 2 size (close to each other) combination`() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)
        val iterator = graph.iterateConnectedCombinations(0, 2)

        val stopCondition = listOf(nodes.by(5), nodes.by(6))
        var items: List<Node<Int>>? = null

        while (iterator.hasNext() &&
            (items == null || items.toMutableList() != stopCondition)
        ) {
            items = iterator.next()
        }
        iterator.remove()
        graph.partialGraphClean()

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(1), nodes.by(3)),
                listOf(nodes.by(5), nodes.by(3)),
            ),
            graph.iterateConnectedCombinations(0, 2).asSequence().toList()
        )

        Assertions.assertEquals(
            listOf<Node<Int>>(),
            graph.iterateConnectedCombinations(0, 2, 3).asSequence().toList()

        )
    }

    @Test
    fun `removing during iteration for case of 2 size (not close to each other) combination`() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)
        val iterator = graph.iterateConnectedCombinations(0, 2)

        val stopCondition = listOf(nodes.by(5), nodes.by(3))
        var items: List<Node<Int>>? = null

        while (iterator.hasNext() &&
            (items == null || items.toMutableList() != stopCondition)
        ) {
            items = iterator.next()
        }
        iterator.remove()
        graph.partialGraphClean()

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(1), nodes.by(3)),
                listOf(nodes.by(5), nodes.by(6)),
                listOf(nodes.by(5), nodes.by(6)),
            ),
            graph.iterateConnectedCombinations(0, 2).asSequence().toList()
        )
    }

    @Test
    fun `removing during iteration for case of 1 size combination`() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)
        val iterator = graph.iterateConnectedCombinations(0)

        iterator.next()
        iterator.remove()
        graph.partialGraphClean()

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(5)),
            ),
            graph.iterateConnectedCombinations(0).asSequence().toList()
        )

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(5), nodes.by(3)),
                listOf(nodes.by(5), nodes.by(6)),
                listOf(nodes.by(5), nodes.by(6)),
            ),
            graph.iterateConnectedCombinations(0, 2).asSequence().toList()
        )

        Assertions.assertTrue(nodes.by(2).getReferenceCountToThisNode() == 1)
    }

    @Test
    fun `removing during iteration for case of 3 size combination`() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)
        val iterator = graph.iterateConnectedCombinations(0, 1, 2)

        val stopCondition = listOf(nodes.by(5), nodes.by(4), nodes.by(6))
        var items: List<Node<Int>>? = null

        while (iterator.hasNext() &&
            (items == null || items.toMutableList() != stopCondition)
        ) {
            items = iterator.next()
        }

        iterator.remove()
        graph.partialGraphClean()

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(1), nodes.by(2), nodes.by(3)),
                listOf(nodes.by(5), nodes.by(2), nodes.by(3)),
            ),
            graph.iterateConnectedCombinations(0, 1, 2).asSequence().toList()
        )
    }

    @Test
    fun `full graph-cleaning of default graph`() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)

        graph.pruneClean()

        Assertions.assertEquals(
            listOf<Node<Int>>(),
            graph.iterateConnectedCombinations(0, 1, 2, 3).asSequence().toList()
        )
    }

    @Test
    fun `full graph-cleaning of specific graph`() {
        val nodes = createNodes().toMutableList().apply {
            add(Node(15, 3, data = 10))
        }
        val graph = initializeGraph(nodes)
        graph.addConnection(nodes.by(3), nodes.by(10))


        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(1), nodes.by(2), nodes.by(3), nodes.by(10)),
                listOf(nodes.by(5), nodes.by(2), nodes.by(3), nodes.by(10)),
            ),
            graph.iterateConnectedCombinations(0, 1, 2, 3).asSequence().toList()
        )

        graph.pruneClean()

        Assertions.assertEquals(
            listOf(
                listOf(nodes.by(1), nodes.by(2), nodes.by(3), nodes.by(10)),
                listOf(nodes.by(5), nodes.by(2), nodes.by(3), nodes.by(10)),
            ),
            graph.iterateConnectedCombinations(0, 1, 2, 3).asSequence().toList()
        )

        graph.printByLayer()
        Assertions.assertEquals(listOf(nodes.by(1), nodes.by(5)), graph.getLayer(0).nodes.toMutableList())
        Assertions.assertEquals(listOf(nodes.by(2)), graph.getLayer(1).nodes.toMutableList())
        Assertions.assertEquals(listOf(nodes.by(3)), graph.getLayer(2).nodes.toMutableList())
        Assertions.assertEquals(listOf(nodes.by(10)), graph.getLayer(3).nodes.toMutableList())

    }

    @Test
    fun hasExpectedConnections() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)

        Assertions.assertTrue(nodes.by(1).connections.contains(nodes.by(2)))

        Assertions.assertTrue(nodes.by(5).connections.contains(nodes.by(2)))
        Assertions.assertTrue(nodes.by(5).connections.contains(nodes.by(4)))
        Assertions.assertTrue(nodes.by(5).connections.contains(nodes.by(6)))

        Assertions.assertTrue(nodes.by(2).connections.contains(nodes.by(3)))
        Assertions.assertTrue(nodes.by(2).connections.contains(nodes.by(8)))
        Assertions.assertTrue(nodes.by(4).connections.contains(nodes.by(6)))

        Assertions.assertTrue(nodes.by(7).connections.contains(nodes.by(8)))

        Assertions.assertTrue(nodes.by(2).getReferenceCountToThisNode() == 2)
        Assertions.assertTrue(nodes.by(6).getReferenceCountToThisNode() == 2)
        Assertions.assertTrue(nodes.by(8).getReferenceCountToThisNode() == 2)
    }


    @Test
    fun layersContainExpectedElements() {
        val nodes = createNodes()
        val graph = initializeGraph(nodes)

        Assertions.assertEquals(
            sortedSetOf(
                nodes.by(1),
                nodes.by(5),
            ),
            graph.getLayer(0).nodes
        )
        Assertions.assertEquals(
            sortedSetOf(
                nodes.by(2),
                nodes.by(4),
                nodes.by(7),
            ),
            graph.getLayer(1).nodes
        )
        Assertions.assertEquals(
            sortedSetOf(
                nodes.by(3),
                nodes.by(6),
                nodes.by(9),
            ),
            graph.getLayer(2).nodes
        )
        Assertions.assertEquals(
            sortedSetOf(
                nodes.by(8),
            ),
            graph.getLayer(3).nodes
        )
    }
}