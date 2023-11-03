package ru.misterpotz.ocgena.dsl

class ObjectTypeCreator(
    private val objectTypesContainer: ObjectTypesContainer,
    private val objectTypeIdCreator: PatternIdCreator,
    private val groupsIdCreator: GroupsIdCreator,
) {
    fun createObjectType(label: String, placeNameCreator: ((placeIndexForType: Long) -> String)?) : ObjectTypeDSL {
        return objectTypesContainer.objectTypes.getOrPut(label) {
            val newId = objectTypeIdCreator.newLabelId()
            val localPlaceNameCreator = placeNameCreator ?: {
                "${label}_$it"
            }
            groupsIdCreator.addPatternIdCreatorFor(label, startIndex = 1, localPlaceNameCreator)
            ObjectTypeImpl(newId, label)
        }
    }
}
