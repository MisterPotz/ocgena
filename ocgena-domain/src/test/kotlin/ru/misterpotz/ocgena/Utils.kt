package ru.misterpotz.ocgena

import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

fun <PerTest> createArgProvider(list: List<PerTest>): ArgumentsProvider {
    return ArgumentsProvider {
        val arrayOfArgs: Array<Arguments> = list.map { Arguments.of(it) }.toTypedArray()
        Stream.of(*arrayOfArgs)
    }
}
