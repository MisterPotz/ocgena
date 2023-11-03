package ru.misterpotz.ocgena.dsl

import dsl.ObjectTypeDSL

interface ObjectTypesContainer {
    val objectTypes : MutableMap<String, ObjectTypeDSL>
}
