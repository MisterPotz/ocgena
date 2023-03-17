package dsl

interface ObjectTypesContainer {
    val objectTypes : MutableMap<String, ObjectTypeDSL>
    val defaultObjectType : ObjectTypeDSL
}
