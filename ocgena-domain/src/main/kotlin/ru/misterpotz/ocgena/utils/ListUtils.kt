package ru.misterpotz.ocgena.utils

inline fun <reified T> List<Any>.findInstance(): T? {
    return find { it is T } as? T?
}