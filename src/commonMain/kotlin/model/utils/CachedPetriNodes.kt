package model.utils

import model.PetriNode

class CachedPetriNodes() {
    private val labelToNode: MutableMap<String, PetriNode> = mutableMapOf()

    fun allNodeLabels(): List<String> {
        return labelToNode.keys.toList()
    }

    fun allNodes() : List<PetriNode> {
        return labelToNode.values.toList()
    }

    fun has(petriNode: PetriNode): Boolean {
        return labelToNode.contains(petriNode.label)
    }

    fun getCachedFor(petriNode: PetriNode): PetriNode? {
        return labelToNode[petriNode.label]
    }


    fun getCachedFor(petriNodeLabel : String): PetriNode? {
        return labelToNode[petriNodeLabel]
    }

    fun save(petriNode: PetriNode): Boolean {
        if (!has(petriNode)) {
            labelToNode[petriNode.label] = petriNode
            return true
        }
        labelToNode[petriNode.label] = petriNode
        return false
    }
}
