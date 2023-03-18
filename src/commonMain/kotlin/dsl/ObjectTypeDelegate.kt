package dsl

class ObjectTypeDelegate(
    var objectTypeCreator : ObjectTypeCreator
) : ObjectTypeAcceptor {

    override fun objectType(label: String, placeNameCreator: ((placeIndexForType: Int) -> String)): ObjectTypeDSL {
        return objectTypeCreator.createObjectType(label, placeNameCreator)
    }

    override fun objectType(label: String): ObjectTypeDSL {
        return objectTypeCreator.createObjectType(label) { "${label}_$it" }
    }
}
