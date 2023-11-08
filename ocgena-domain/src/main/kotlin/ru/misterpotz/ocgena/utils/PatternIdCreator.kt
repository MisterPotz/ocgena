package ru.misterpotz.ocgena.utils

class PatternIdCreator(val namer: (Long) -> String) {
    var index: Long = 0

    val newIntId: Long
        get() {
            return index++
        }
    val lastLabelId: String
        get() {
            return namer(index - 1)
        }
}
