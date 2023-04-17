package model.utils

import model.AbsPetriAtomVisitorDFS
import model.Place
import model.Transition

class NodesCacherVisitorDFS(
    private val cachedPetriNodes: CachedPetriNodes = CachedPetriNodes(),
) : AbsPetriAtomVisitorDFS() {

    override fun doForTransitionBeforeDFS(transition: Transition): Boolean {
        return cachedPetriNodes.save(transition)
    }

    override fun doForPlaceBeforeDFS(place: Place): Boolean {
        return cachedPetriNodes.save(place)
    }

    fun getCachedNodes() : CachedPetriNodes {
        return cachedPetriNodes
    }
}
