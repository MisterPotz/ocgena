package ru.misterpotz.ocgena.simulation_v2.algos

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.*

class CombinatorTest {
    @Test
    fun combinatorTest() {
        val first = CombinationIterable<Int>(listOf(11, 12, 13, 14), combinationSize = 2)
        val second = CombinationIterable<Int>(listOf(21, 22, 23), combinationSize = 1)
        val third = CombinationIterable<Int>(listOf(31, 32, 33, 34, 35, 36), combinationSize = 3)
        val fourth = CombinationIterable<Int>(listOf(41, 42), combinationSize = 1)

        val combination = IteratorsCombinator(
            listOf(first, second, third, fourth)
        )


        for (i in combination) {
            println(i)
        }
        Assertions.assertEquals(6 * 3 * 20 * 2, combination.count())
    }

    @Test
    fun combinatorTest2() {
        val first = CombinationIterable<Int>((0..<3).toList(), combinationSize = 1)
        val second = CombinationIterable<Int>((0..<7).toList(), combinationSize = 1)
        val third = CombinationIterable<Int>((0..<7).toList(), combinationSize = 1)

        val combination = IteratorsCombinator(
            listOf(first, second, third)
        )

        for (i in combination) {
            println(i)
        }
        Assertions.assertEquals(3 * 7 * 7, combination.count())
    }

    @Test
    fun filteredIterableTest() {
        val filtered =
            FilteredIterator(listOf(0, 2), listOf(listOf(1, 2, 3), listOf(1000, 1001, 1002), listOf(4, 5, 6)))

        Assertions.assertEquals(listOf(1, 2, 3, 4, 5, 6), filtered.asSequence().toList())
    }

    @Test
    fun filteredIterableTest1() {
        val filtered =
            FilteredIterator(listOf(), listOf(listOf(1, 2, 3), listOf(1000, 1001, 1002), listOf(4, 5, 6)))

        Assertions.assertEquals(emptyList<Int>(), filtered.asSequence().toList())
    }

    @Test
    fun filteredIterableTest2() {
        val filtered =
            FilteredIterator(listOf(1), listOf(listOf(1, 2, 3), listOf(1000, 1001, 1002), listOf(4, 5, 6)))

        Assertions.assertEquals(listOf<Int>(1000, 1001, 1002), filtered.asSequence().toList())
    }

    @Test
    fun controlledIterationTest() {
        val iterables = listOf(
            listOf(1, 2, 3).asIterable(),
            listOf("a", "b").asIterable(),
            listOf(false, true).asIterable(), // Non-empty iterable
            listOf(4.5, 5.5).asIterable()
        )

        val combinator = ControlledIteratorsCombinator(iterables)

        val iterator = combinator.iterator()

        val solutions = iterator.asSequence().toList()
        println(solutions)

        Assertions.assertEquals(3 * 2 * 2 * 2, solutions.size)
    }

    @Test
    fun iterationTest2() {
        val iterables = listOf(
            listOf(1, 2, 3).asIterable(),
            listOf("a", "b").asIterable(),
            listOf(false, true).asIterable(), // Non-empty iterable
            listOf(4.5, 5.5).asIterable()
        )

        val combinator = ControlledIteratorsCombinator(iterables)

        val iterator = combinator.iterator()

        val first = iterator.next()

        combinator.advanceIndex(1)
        val second = iterator.next()
        combinator.advanceIndex(2)
        val third = iterator.next()
        combinator.advanceIndex(0)
        val fourth = iterator.next()
        val fifth = iterator.next()
        println(first)
        println(second)
        println(third)
        println(fourth)
        println(fifth)

        val solutions = iterator.asSequence().toList()
        println(solutions)
        Assertions.assertEquals(14, solutions.size)
    }
}