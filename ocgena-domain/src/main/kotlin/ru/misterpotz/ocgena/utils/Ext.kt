package ru.misterpotz.ocgena.utils

@Suppress("UNCHECKED_CAST")
fun <R> Any.cast(): R {
    return this as R
}