package dsl

interface ObjectTypesContainer {
    val objectTypes : MutableMap<String, ObjectTypeDSL>
}
