package model

import model.utils.NodesCacherVisitorDFS
import model.utils.PetriNodesCopyTask
import simulation.Marking

/**
 * the net, formed with passed arguments, must already be consistent
 */
data class WellFormedOCNet(
    val inputPlaces: List<Place>,
    val outputPlaces: List<Place>,
    override val places: List<Place>,
    override val transitions: List<Transition>,
    override val objectTypes: List<ObjectType>,
    override val arcs: List<Arc>,
    override val allPetriNodes: List<PetriNode>,
) : OCNetElements {

    override fun toString(): String {
        return "OCNet(inputPlaces=$inputPlaces, outputPlaces=$outputPlaces, objectTypes=$objectTypes)"
    }

    private val labelToNode: MutableMap<String, PetriNode> = mutableMapOf()

    fun strictSetMarking(marking: Marking) {
        for (i in places) {
            val tokens = marking.getStrictMarkingFor(i.label)
            i.tokens = tokens
        }
    }

    fun weakSetMarking(marking: Marking) {
        for (i in places) {
            val tokens = marking.getWeakMarkingFor(i.label)
            if (tokens != null) {
                i.tokens = tokens
            }
        }
    }

    fun fullCopy(): WellFormedOCNet {
        val allNodesCacher = NodesCacherVisitorDFS()
        allNodesCacher.collectAllNodes(this)
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
            places = copied.allNodes().filterIsInstance<Place>(),
            transitions = copied.allNodes().filterIsInstance<Transition>(),
            objectTypes = objectTypes,
            arcs = arcs,
            allPetriNodes = allPetriNodes
        )
    }
}
