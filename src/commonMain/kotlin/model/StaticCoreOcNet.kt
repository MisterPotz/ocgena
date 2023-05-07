package model

import model.utils.NodesCacherVisitorDFS
import model.utils.PetriNodesCopyTask
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


/**
 * the net, formed with passed arguments, must already be consistent
 */
@OptIn(ExperimentalJsExport::class)
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
class StaticCoreOcNet(
    val inputPlaces: Places,
    val outputPlaces: Places,
    override val places: Places,
    override val transitions: Transitions,
    override val objectTypes: ObjectTypes,
    override val arcs: Arcs,
    override val allPetriNodes: List<PetriNode>,
    override val placeTyping: PlaceTyping,
) : OCNetElements {

    override fun toString(): String {
        return "OCNet(inputPlaces=$inputPlaces, outputPlaces=$outputPlaces, objectTypes=$objectTypes)"
    }

    private val labelToNode: MutableMap<String, PetriNode> = mutableMapOf()

    fun fullCopy(): StaticCoreOcNet {
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
        return StaticCoreOcNet(
            inputPlaces = Places(places = createdInputPlaces),
            outputPlaces = Places(createdOutputPlaces),
            places = Places(copied.allNodes().filterIsInstance<Place>()),
            transitions = Transitions(copied.allNodes().filterIsInstance<Transition>()),
            objectTypes = objectTypes,
            arcs = arcs,
            allPetriNodes = allPetriNodes,
            placeTyping = placeTyping,
        )
    }
}

