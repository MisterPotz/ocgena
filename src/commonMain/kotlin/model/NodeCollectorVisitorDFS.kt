package model

class PlaceCollectorVisitorDFS : AbsPetriAtomVisitorDFS() {
    val places = mutableListOf<Place>()

    override fun doForPlaceBeforeDFS(place: Place): Boolean {

        return false
    }
}
