package model

class Places(val places: List<Place>) : List<Place> by places {

    operator fun get(placeId : String) : Place {
        return places.find { it.id == placeId }!!
    }
    fun reindexArcs() {
        for (place in places) {
            place.reindexArcs()
        }
    }
}
