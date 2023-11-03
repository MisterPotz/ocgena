package ru.misterpotz.ocgena.dsl

interface PlacesContainer {
    val places: MutableMap<String, PlaceDSL>
}
