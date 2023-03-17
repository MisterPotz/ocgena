package dsl

class PlaceDelegate(
    var placeCreator: PlaceCreator?
) : PlaceAcceptor {
    private val _placeCreator: PlaceCreator
        get() = placeCreator!!
    override fun place(label: String): PlaceDSL {
        return _placeCreator.createPlace(label) { }
    }

    override fun place(block: OCPlaceScope.() -> Unit): PlaceDSL {
        return _placeCreator.createPlace(label = null, block)
    }

    override fun place(label: String, block: OCPlaceScope.() -> Unit): PlaceDSL {
        return _placeCreator.createPlace(label, block)
    }

}
