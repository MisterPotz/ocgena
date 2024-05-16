package ru.misterpotz.ocgena.utils

class PatternIdCreator(
    private val nextIdProvider: (() -> Long)? = null,
    val namer: (Long) -> String
) {
    var index: Long = 0

    val newIntId: Long
        get() {
            return nextIdProvider?.invoke() ?: index++
        }
    val lastLabelId: String
        get() {
            return namer(index - 1)
        }
}
