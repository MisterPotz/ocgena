package ru.misterpotz.ocgena.utils

fun <T : Comparable<T>> mergeSortedCollections(coll1: Iterable<T>, coll2: Iterable<T>): List<T> {
    val result = mutableListOf<T>()
    val iter1 = coll1.iterator()
    val iter2 = coll2.iterator()

    var val1: T? = if (iter1.hasNext()) iter1.next() else null
    var val2: T? = if (iter2.hasNext()) iter2.next() else null

    while (val1 != null || val2 != null) {
        when {
            val1 == null -> {
                result.add(val2!!)
                val2 = if (iter2.hasNext()) iter2.next() else null
            }

            val2 == null -> {
                result.add(val1)
                val1 = if (iter1.hasNext()) iter1.next() else null
            }

            val1 < val2 -> {
                result.add(val1)
                val1 = if (iter1.hasNext()) iter1.next() else null
            }

            else -> {
                result.add(val2)
                val2 = if (iter2.hasNext()) iter2.next() else null
            }
        }
    }

    return result
}

fun <T : Comparable<T>> MutableList<T>.sortedInsert(item : T)  {
    var i = size - 1
    while (i >= 0) {
        if (this[i] < item) {
            add(i + 1, item)
            return
        }
        i--
    }
    add(0, item)
}