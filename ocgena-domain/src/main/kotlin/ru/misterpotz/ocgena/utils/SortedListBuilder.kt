package ru.misterpotz.ocgena.utils

fun <T> buildSortedList(list: MutableList<T>.() -> Unit): List<T> {
    return buildList {
        list()
    }
}

class Block<T : Comparable<T>>(val list: MutableList<T> = mutableListOf()) : MutableList<T> by list {
    override fun add(element: T): Boolean {
        sortedInsert(element)
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        list.addAll(elements.sorted())
        return true
    }
}
