package model

/**
 * the net, formed with passed arguments, must already be consistent
 */
data class WellFormedOCNet(
    val inputPlaces: List<Place>,
    val outputPlaces: List<Place>,
    val objectTypes: List<ObjectType>,
) {

    override fun toString(): String {
        return "OCNet(inputPlaces=$inputPlaces, outputPlaces=$outputPlaces, objectTypes=$objectTypes)"
    }
}
