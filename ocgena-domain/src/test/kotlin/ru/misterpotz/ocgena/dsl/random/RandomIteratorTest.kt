package ru.misterpotz.ocgena.dsl.random

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.ocgena.utils.ByRandomRandomizer
import ru.misterpotz.ocgena.utils.RandomIterator
import java.util.stream.Stream
import kotlin.random.Random

class RandomIteratorTest {

    @ParameterizedTest()
    @ArgumentsSource(ArgsProvider::class)
    fun producesUniqueKeys(size : Int) {
        val randomIterator = RandomIterator(amount = size, randomizer = ByRandomRandomizer(random = Random(42)))

        val verificationSet = mutableSetOf<Int>()

        while (randomIterator.hasNext()) {
            val next = randomIterator.next()
            Assertions.assertTrue(next !in verificationSet)
            verificationSet.add(next)
        }
        Assertions.assertTrue(verificationSet.size == size)
    }

    object ArgsProvider : ArgumentsProvider {
        class Args(val arr: Array<Any>) : Arguments {
            override fun get(): Array<Any> {
                return arr
            }
        }

        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return listOf(
                Args(arrayOf(1)),
                Args(arrayOf(10)),
                Args(arrayOf(100)),
                Args(arrayOf(10000)),
            ).stream()
        }
    }
}