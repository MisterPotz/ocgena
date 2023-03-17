package dsl

class ObjectTypeDelegate(
    var lateinitObjectTypeCreator : ObjectTypeCreator? = null
) : ObjectTypeAcceptor {
    val objectTypeCreator
        get() = lateinitObjectTypeCreator!!

    override fun objectType(label: String, placeNameCreator: ((placeIndexForType: Int) -> String)): ObjectTypeDSL {
        return objectTypeCreator.createObjectType(label, placeNameCreator)
    }

    override fun objectType(label: String): ObjectTypeDSL {
        return objectTypeCreator.createObjectType(label) { "${label}_$it" }
    }
}
