package ru.misterpotz.ocgena.dsl

class PlaceDSLCreation(
    val userPlaceLabel : String?,
    val groupIdIssuer: GroupsIdCreator,
    var defaultIdForObjectType: Long,
    var defaultLabelIdForObjectType: String,
    var selectedObjectType: ObjectTypeDSL
)  {
    private val idCreatorForSelectedObjType: PatternIdCreator
        get() = groupIdIssuer.patternIdCreatorFor(selectedObjectType.label)
    private var createdPlace : PlaceDSL? = null

    private fun onPlaceAssignedNewObjectType(newObjectTypeDSL: ObjectTypeDSL) {
        idCreatorForSelectedObjType.removeLast()
        selectedObjectType = newObjectTypeDSL
        defaultIdForObjectType = idCreatorForSelectedObjType.newIntId()
        defaultLabelIdForObjectType = idCreatorForSelectedObjType.lastLabelId
    }

    private fun getCurrentPlaceLabel() : String {
        return userPlaceLabel ?: defaultLabelIdForObjectType
    }

    fun createPlaceDSLWithBlock(block: PlaceDSL.() -> Unit) : PlaceDSL {
        createdPlace?.let { return it }

        val newPlace = PlaceDSLImpl(
            objectTypeId = defaultIdForObjectType,
            onAssignNewObjectType = {
                onPlaceAssignedNewObjectType(it)
            },
            labelFactory = {
                getCurrentPlaceLabel()
            },
            objectType = selectedObjectType
        ).also {
            createdPlace = it
        }

        newPlace.block()
        newPlace.objectTypeId = defaultIdForObjectType
        newPlace.finalLabel = getCurrentPlaceLabel()
        return newPlace
    }

    companion object {
        fun createFromIdIssuerAndScopeType(
            userPlaceLabel : String?,
            groupIdCreator: GroupsIdCreator,
            scopeType: ObjectTypeDSL
        ) : PlaceDSLCreation {
            val placeIdIssuer = groupIdCreator.patternIdCreatorFor(scopeType.label)
            val defaultId = placeIdIssuer.newIntId()
            val defaultLabelId = placeIdIssuer.lastLabelId
            return PlaceDSLCreation(
                userPlaceLabel = userPlaceLabel,
                groupIdIssuer = groupIdCreator,
                defaultIdForObjectType = defaultId,
                defaultLabelIdForObjectType = defaultLabelId,
                selectedObjectType = scopeType
            )
        }
    }
}
