package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper

class FilteredIterator<T>(val sublistIndices: List<Int>, val originalListOfLists: List<List<T>>) :
    Iterable<T> {

    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            private var currentListIndex = 0
            private var currentSublistIndex = 0

            init {
                // Move to the first valid sublist
                moveToNextValidSublist()
            }

            override fun hasNext(): Boolean {
                // Check if there is a next token in the current sublist
                return currentListIndex < sublistIndices.size &&
                        currentSublistIndex < originalListOfLists[sublistIndices[currentListIndex]].size
            }

            override fun next(): T {
                if (!hasNext()) {
                    throw NoSuchElementException("No more elements")
                }
                val actualListIndex = sublistIndices[currentListIndex]
                val currentSublist = originalListOfLists[actualListIndex]
                val token = currentSublist[currentSublistIndex]
                currentSublistIndex++
                moveToNextValidSublist()
                return token
            }

            private fun moveToNextValidSublist() {
                while (currentListIndex < sublistIndices.size) {
                    val actualListIndex = sublistIndices[currentListIndex]
                    if (actualListIndex < originalListOfLists.size) {
                        val currentSublist = originalListOfLists[actualListIndex]
                        if (currentSublistIndex < currentSublist.size) {
                            return
                        }
                        currentSublistIndex = 0
                    }
                    currentListIndex++
                }
            }
        }
    }
}