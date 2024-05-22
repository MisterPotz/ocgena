package ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search

import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper

class FilteredIterator(val sublistIndices: List<Int>, val originalListOfLists: List<List<TokenWrapper>>) :
    Iterable<TokenWrapper> {

    override fun iterator(): Iterator<TokenWrapper> {
        return object : Iterator<TokenWrapper> {
            private var currentListIndex = 0
            private var currentSublistIndex = 0

            init {
                // Move to the first valid sublist
                moveToNextValidSublist()
            }

            override fun hasNext(): Boolean {
                // Check if there is a next token in the current sublist
                while (currentListIndex < sublistIndices.size) {
                    val actualListIndex = sublistIndices[currentListIndex]
                    val currentSublist = originalListOfLists[actualListIndex]
                    if (currentSublistIndex < currentSublist.size) {
                        return true
                    } else {
                        // Move to the next valid sublist
                        moveToNextValidSublist()
                    }
                }
                return false
            }

            override fun next(): TokenWrapper {
                if (!hasNext()) {
                    throw NoSuchElementException("No more elements")
                }
                val actualListIndex = sublistIndices[currentListIndex]
                val currentSublist = originalListOfLists[actualListIndex]
                val token = currentSublist[currentSublistIndex]
                currentSublistIndex++
                return token
            }

            private fun moveToNextValidSublist() {
                currentSublistIndex = 0
                while (currentListIndex < sublistIndices.size) {
                    val actualListIndex = sublistIndices[currentListIndex]
                    if (actualListIndex < originalListOfLists.size) {
                        val currentSublist = originalListOfLists[actualListIndex]
                        if (currentSublist.isNotEmpty()) {
                            break
                        }
                    }
                    currentListIndex++
                }
            }
        }
    }
}