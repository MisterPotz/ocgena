package model

class TokenInitializerVisitorDFS : AbsPetriAtomVisitorDFS() {
    override fun doForPlaceBeforeDFS(place: Place): Boolean {
        place.tokens = place.initialTokens ?: 0
        return false
    }
}
