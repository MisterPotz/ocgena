package ru.misterpotz.ocgena.utils

@Suppress("UNCHECKED_CAST")
fun <R> Any.cast(): R {
    return this as R
}

fun <T> T?.LOG(block: ((T) -> String)? = null): T? {
    println(
        String.format(
            "%-50s | %s",
            "WTFAAA ${this?.let { block?.invoke(this) } ?: ""}", "$this"
        )
    )
    return this
}
