package ru.misterpotz.ocgena.simulation_v2.algos

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.CombinationIterableInt
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.RandomLeveledCombinationIterator
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.Shuffler
import ru.misterpotz.ocgena.simulation_v2.utils.factorial

class LayeredRandomIterationTest {

    class DeterminedShuffler(vararg sequence: List<Int>) : Shuffler {
        val iterator = sequence.iterator()

        override fun makeShuffled(intRange: IntRange): List<Int> {
            return iterator.next()
        }

        override fun select(ints: List<Int>): Int {
            require(ints.isNotEmpty()) { "cannot select random from an empty list" }
            return ints.first()
        }

        override fun select(longs: LongRange): Long {
            return iterator.next().first().toLong()
        }

        override fun select(indices: IntRange): Int {
            return iterator.next().first()
        }
    }

    @Test
    fun simpleCombinator() {
        val iterator = CombinationIterableInt(
            indicesToVisit = listOf(1, 5, 10),
            2
        )

        Assertions.assertEquals(
            listOf(
                listOf(1, 5),
                listOf(1, 10),
                listOf(5, 10),
            ),

            iterator.asSequence().toList()
        )

        val other = CombinationIterableInt(listOf(1, 5, 10, 20, 25, 30), 3)
        val combinatorics = 6.factorial() / (3.factorial() * (6 - 3).factorial())

        Assertions.assertEquals(combinatorics, other.asSequence().toList().size)

        val other1 = CombinationIterableInt(listOf(), 1)
        Assertions.assertEquals(0, other1.asSequence().toList().size)

        val other2 = CombinationIterableInt(listOf(1), 2)
        Assertions.assertEquals(0, other2.asSequence().toList().size)

        val other3 = CombinationIterableInt(listOf(1), 1)
        Assertions.assertEquals(1, other3.asSequence().toList().size)
    }

    @Test
    fun edgecases() {
        val ranges = listOf(
            0..1,
            0..2,
            3..5
        )

        val testShuffler = DeterminedShuffler(
            listOf(1, 0),
            listOf(1, 0, 2),
            listOf(5, 3, 4)
        )

        val randomLeveledCombinationIterator = RandomLeveledCombinationIterator(
            ranges,
            nodesAtLevel = listOf(1, 2, 2),
            testShuffler,
            mode = RandomLeveledCombinationIterator.Mode.BFS
        )

        val combinatorics = (2 * 1) * ( /*3 числа 2 места*/ 3) * (3 /* same */)

        Assertions.assertEquals(combinatorics, randomLeveledCombinationIterator.asSequence().toList().size)
    }


    @Test
    fun edgecases2() {
        val ranges = listOf(
            0..1,
            0..2,
            3..5
        )

        val testShuffler = DeterminedShuffler(
            listOf(1, 0),
            listOf(1),
            listOf(5, 3, 4)
        )

        val randomLeveledCombinationIterator = RandomLeveledCombinationIterator(
            ranges,
            nodesAtLevel = listOf(1, 1, 2),
            testShuffler,
            mode = RandomLeveledCombinationIterator.Mode.BFS
        )

        Assertions.assertEquals(
            listOf(
                listOf(listOf(1), listOf(1), listOf(5, 3)),
                listOf(listOf(0), listOf(1), listOf(5, 3)),
                listOf(listOf(1), listOf(1), listOf(5, 4)),
                listOf(listOf(0), listOf(1), listOf(5, 4)),
                listOf(listOf(1), listOf(1), listOf(3, 4)),
                listOf(listOf(0), listOf(1), listOf(3, 4)),
            ),
            randomLeveledCombinationIterator.asSequence().toList()
        )
    }
}