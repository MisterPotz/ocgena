package ru.misterpotz.model.ext

import model.PetriNode

fun <K, V, T> Map<K, V>.copyWithValueTransform(transform: (v: V) -> T): Map<K, T> {
    return mapValues {
        transform(it.value)
    }
}


fun <K, V, T> Map<K, V>.copyWithValueTransformMutable(transform: (v: V) -> T): MutableMap<K, T> {
    return mapValuesTo(mutableMapOf()) {
        transform(it.value)
    }
}

fun <T : PetriNode> List<T>.sortById(): List<T> {
    return sortedBy { it.id }
}