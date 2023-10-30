package dsl

interface PlacesContainer {
    val places: MutableMap<String, PlaceDSL>
}
