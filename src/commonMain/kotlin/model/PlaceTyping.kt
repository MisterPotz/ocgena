package model

class PlaceTyping() {


    operator fun get(place: Place): ObjectType {
        return place.type
    }
}
