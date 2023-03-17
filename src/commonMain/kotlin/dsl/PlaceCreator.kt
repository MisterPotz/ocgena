package dsl

class PlaceCreator(
    private val scopeType: ObjectTypeDSL,
    private val placesContainer: PlacesContainer,
    private val groupIdIssuer: GroupsIdCreator
) {
    val places : MutableMap<String, PlaceDSL>
        get() = placesContainer.places

    fun findExistingPlace(label : String?) : PlaceDSL? {
        if (label != null) {
            val place = places[label]
            if (place != null) {
                return place
            }
        }
        return null
    }

    fun createPlace(label: String? = null, block: OCPlaceScope.() -> Unit): PlaceDSL {
        val existingPlace = findExistingPlace(label)
        if (existingPlace != null) {
            return existingPlace
        }

        val placeDSLCreation = PlaceDSLCreation.createFromIdIssuerAndScopeType(
            groupIdCreator = groupIdIssuer,
            scopeType = scopeType,
            userPlaceLabel = label
        )

        val placeDSL = placeDSLCreation.createPlaceDSLWithBlock(block)
        places[placeDSL.label] = (placeDSL)
        return placeDSL
    }
}
