package dsl

class ObjectTypeCreator(
    private val objectTypesContainer: ObjectTypesContainer,
    private val objectTypeIdCreator: PatternIdCreator,
    private val groupsIdCreator: GroupsIdCreator,
) {
    fun createObjectType(label: String, placeNameCreator: ((placeIndexForType: Int) -> String)) : ObjectTypeDSL {
        return objectTypesContainer.objectTypes.getOrPut(label) {
            val newId = objectTypeIdCreator.newIntId()
            groupsIdCreator.addPatternIdCreatorFor(label, startIndex = 1) { "${label}_$it" }
            ObjectTypeImpl(newId, label)
        }
    }
}
