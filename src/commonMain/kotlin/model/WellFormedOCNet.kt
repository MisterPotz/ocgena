package model

import model.utils.NodesCacherVisitorDFS
import model.utils.PetriNodesCopyTask

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

    private val labelToNode : MutableMap<String, PetriNode> = mutableMapOf()

    fun fullCopy() : WellFormedOCNet {
        val allNodesCacher = NodesCacherVisitorDFS()
        for (inputPlace in inputPlaces) {
            allNodesCacher.visitPlace(inputPlace)
        }
        val cachedNodes = allNodesCacher.getCachedNodes()
        val copyTask = PetriNodesCopyTask(cachedPetriNodes = cachedNodes)
        val copied = copyTask.performAndGetCopiedNodes()

        val createdInputPlaces = inputPlaces.map {
            copied.getCachedFor(it) as Place
        }
        val createdOutputPlaces = outputPlaces.map {
            copied.getCachedFor(it) as Place
        }
        return WellFormedOCNet(
            createdInputPlaces,
            createdOutputPlaces,
            objectTypes
        )
    }
}
