package model

class Places(val places: List<Place>) : List<Place> by places {
    fun reindexArcs() {
        for (place in places) {
            place.reindexArcs()
        }
    }
}
