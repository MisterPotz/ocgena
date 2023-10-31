package model.utils

import model.AbsPetriAtomVisitorDFS
import model.Place
import model.Transition
import model.StaticCoreOcNet

class NodesCacherVisitorDFS(
    private val cachedPetriNodes: CachedPetriNodes = CachedPetriNodes(),
) : AbsPetriAtomVisitorDFS() {

    fun collectAllNodes(wellFormedOCNet: StaticCoreOcNet) {
        for (i in wellFormedOCNet.inputPlaces) {
            visitPlace(i)
        }
    }

    override fun doForTransitionBeforeDFS(transition: Transition): Boolean {
        return !cachedPetriNodes.save(transition)
    }

    override fun doForPlaceBeforeDFS(place: Place): Boolean {
        return !cachedPetriNodes.save(place)
    }

    fun getCachedNodes() : CachedPetriNodes {
        return cachedPetriNodes
    }
}