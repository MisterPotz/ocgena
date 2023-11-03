package ru.misterpotz.ocgena.dsl

interface ObjectTypesContainer {
    val objectTypes : MutableMap<String, ObjectTypeDSL>
}
